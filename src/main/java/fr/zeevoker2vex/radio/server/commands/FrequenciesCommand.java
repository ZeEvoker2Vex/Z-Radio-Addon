package fr.zeevoker2vex.radio.server.commands;

import fr.zeevoker2vex.radio.common.utils.CommandHelpBuilder;
import fr.zeevoker2vex.radio.common.utils.MessageBuilder;
import fr.zeevoker2vex.radio.server.ServerProxy;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;

public class FrequenciesCommand extends CommandBase {

    private final HashMap<String, Integer> commandArguments = new HashMap<>();
    private final List<String> frequenciesArguments = new ArrayList<>();
    public FrequenciesCommand(){
        // Argument, Details
        commandArguments.put("view", 0);
        commandArguments.put("blacklisted", 3);
        commandArguments.put("restricted", 7);
        commandArguments.put("help", 0);
        // Blacklist & Restricted Arg
        frequenciesArguments.add("add");
        frequenciesArguments.add("remove");
        frequenciesArguments.add("view");
    }

    @Override
    public String getName() {
        return "frequencies";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "§cUsage: /frequencies <view/blacklisted/restricted/help> [add/remove/view] [frequency] [player]\n§cSee /frequencies help for details.";
    }

    /**
        /frequencies <view/blacklisted/restricted/help> <add/remove/view> <frequency> [player]
        => view : voir toutes les fréquences ([Restreinte])
        => blacklisted/restricted view : voir les radios blacklists ou restreintes (ça dit pour voir les joueurs wl ajouter une freq)
                                => add <freq> : ajouter une fréquence à celles bl/wl
                                => remove <freq> : retirer une fréquence à celles bl/wl
        => restricted view <freq> : voir que la fréquence + les joueurs wl pour les restreintes
        => restricted add/remove <freq> <player> : ajouter/supprimer un joueur à la wl de cette fréquence
     */

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        MessageBuilder.ComponentMessage message = new MessageBuilder.ComponentMessage("");
        if(args.length==1){
            if(args[0].equals("view")){
                HashMap<Short, List<String>> frequencies = new HashMap<>();

                for(Map.Entry<EntityPlayer, HashMap<UUID, Short>> entry : RadioManager.playersFrequencies.entrySet()) {
                    for(short frequency : entry.getValue().values()){
                        List<String> players = frequencies.getOrDefault(frequency, new ArrayList<>());
                        players.add(entry.getKey().getName());

                        frequencies.put(frequency, players);
                    }
                }
                if(!frequencies.isEmpty()) {
                    message = message.reset("§e").addTranslation("cmd.frequencies.view.title");

                    for(Map.Entry<Short, List<String>> entry : frequencies.entrySet()){
                        short frequency = entry.getKey();
                        message = message.addLine("§6=> "+frequency+" Mhz");
                        if(ServerProxy.getConfig().isRestricted(frequency)) message = message.addString("§c").addTranslation("cmd.frequencies.view.restricted");
                        else if(ServerProxy.getConfig().isBlacklisted(frequency)) message = message.addString("§c").addTranslation("cmd.frequencies.view.blacklisted");
                        message = message.addString(" §f:");

                        List<String> playerNames = entry.getValue();
                        int listSize = playerNames.size();
                        for (int i = 0; i < listSize; i++) {
                            String playerName = playerNames.get(i);
                            String suffix = i + 1 == listSize ? "§f." : "§f,";
                            message = message.addString(" §7"+playerName+suffix);
                        }
                    }
                }
                else message = message.reset("§e").addTranslation("cmd.frequencies.view.none");

                message.sendTo(sender);
                return;
            }
            else if(args[0].equals("help")){
                CommandHelpBuilder commandHelpBuilder = new CommandHelpBuilder(getName(), new ArrayList<>(commandArguments.keySet()));
                commandHelpBuilder.build().sendTo(sender);
                return;
            }
        }
        else if(args.length==2){
            if(args[0].equals("help")){
                String detailedArg = args[1];
                if(this.commandArguments.containsKey(detailedArg)){
                    CommandHelpBuilder.ArgumentBuilder argumentBuilder = new CommandHelpBuilder.ArgumentBuilder(getName(), detailedArg, this.commandArguments.get(detailedArg));
                    argumentBuilder.build().sendTo(sender);
                }
                else message.reset("§c").addTranslation("cmd.frequencies.help.details.failed", detailedArg, getName()).sendTo(sender);
                return;
            }
            else if(args[0].equals("blacklisted")){
                if(args[1].equals("view")){
                    List<Short> blacklistedFrequencies = ServerProxy.getConfig().getBlacklistedFrequencies();

                    if(!blacklistedFrequencies.isEmpty()){
                        message = message.reset("§e").addTranslation("cmd.frequencies.blacklisted.view.title");

                        int listSize = blacklistedFrequencies.size();
                        for(int i = 0; i < listSize; i++){
                            short blacklistedFrequency = blacklistedFrequencies.get(i);
                            String suffix = i+1==listSize ? "§f." : "§f,";
                            message = message.addString(" §6"+blacklistedFrequency+suffix);
                        }
                    }
                    else message = message.reset("§e").addTranslation("cmd.frequencies.blacklisted.view.none");

                    message.sendTo(sender);
                    return;
                }
            }
            else if(args[0].equals("restricted")){
                if(args[1].equals("view")){
                    List<Short> restrictedFrequencies = new ArrayList<>(ServerProxy.getConfig().getRestrictedFrequencies().keySet());

                    if(!restrictedFrequencies.isEmpty()){
                        message = message.reset("§e").addTranslation("cmd.frequencies.restricted.view.title");

                        int listSize = restrictedFrequencies.size();
                        for(int i = 0; i < listSize; i++){
                            short blacklistedFrequency = restrictedFrequencies.get(i);
                            String suffix = i+1==listSize ? "§f." : "§f,";
                            message = message.addString(" §6"+blacklistedFrequency+suffix);
                        }
                    }
                    else message = message.reset("§e").addTranslation("cmd.frequencies.restricted.view.none");

                    message.sendTo(sender);
                    return;
                }
            }
        }
        else if(args.length==3){
            short frequency;
            try {
                frequency = Short.parseShort(args[2]);
            }
            catch(NumberFormatException exception){
                message.reset("§c").addTranslation("cmd.frequencies.invalidFrequency");
                message.sendTo(sender);
                return;
            }
            if(args[0].equals("blacklisted")){
                if(args[1].equals("add")){
                    if(ServerProxy.getConfig().isBlacklisted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.blacklisted.add.already");
                    else if(ServerProxy.getConfig().isRestricted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.blacklisted.add.restricted");
                    else {
                        if(ServerProxy.getConfig().addToBlacklist(frequency)) message.reset("§2").addTranslation("cmd.frequencies.blacklisted.add.success", frequency);
                        else message.reset("§c").addTranslation("cmd.frequencies.blacklisted.add.failed", frequency);
                    }
                    message.sendTo(sender);
                    return;
                }
                else if(args[1].equals("remove")){
                    if(!ServerProxy.getConfig().isBlacklisted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.blacklisted.remove.none");
                    else {
                        if(ServerProxy.getConfig().removeFromBlacklist(frequency)) message.reset("§2").addTranslation("cmd.frequencies.blacklisted.remove.success", frequency);
                        else message.reset("§c").addTranslation("cmd.frequencies.blacklisted.remove.failed", frequency);
                    }
                    message.sendTo(sender);
                    return;
                }
            }
            else if(args[0].equals("restricted")){
                if(args[1].equals("add")){
                    if(ServerProxy.getConfig().isRestricted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.add.already");
                    else if(ServerProxy.getConfig().isBlacklisted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.add.blacklisted");
                    else {
                        if(ServerProxy.getConfig().addToRestricted(frequency)) message.reset("§2").addTranslation("cmd.frequencies.restricted.add.success", frequency);
                        else message.reset("§c").addTranslation("cmd.frequencies.restricted.add.failed", frequency);
                    }
                    message.sendTo(sender);
                    return;
                }
                else if(args[1].equals("remove")){
                    if(!ServerProxy.getConfig().isRestricted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.none");
                    else {
                        if(ServerProxy.getConfig().removeFromRestricted(frequency)) message.reset("§2").addTranslation("cmd.frequencies.restricted.remove.success", frequency);
                        else message.reset("§c").addTranslation("cmd.frequencies.restricted.remove.failed", frequency);
                    }
                    message.sendTo(sender);
                    return;
                }
                else if(args[1].equals("view")){
                    if(!ServerProxy.getConfig().isRestricted(frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.none");
                    else {
                        List<String> playersName = ServerProxy.getConfig().getPlayerNames(frequency);

                        if(!playersName.isEmpty()){
                            message = message.reset("§e").addTranslation("cmd.frequencies.restricted.view.freq.title", frequency);

                            int listSize = playersName.size();
                            for(int i = 0; i < listSize; i++){
                                String playerName = playersName.get(i);
                                String suffix = i+1==listSize ? "§f." : "§f,";
                                message = message.addString(" §7"+playerName+suffix);
                            }
                        }
                        else message = message.reset("§e").addTranslation("cmd.frequencies.restricted.view.freq.none");
                    }
                    message.sendTo(sender);
                    return;
                }
            }
        }
        else if(args.length==4){
            short frequency;
            try {
                frequency = Short.parseShort(args[2]);
            }
            catch(NumberFormatException exception){
                message.reset("§c").addTranslation("cmd.frequencies.invalidFrequency");
                message.sendTo(sender);
                return;
            }
            if(!ServerProxy.getConfig().isRestricted(frequency)) {
                message.reset("§c").addTranslation("cmd.frequencies.restricted.none");
                message.sendTo(sender);
                return;
            }
            EntityPlayer target = sender.getEntityWorld().getPlayerEntityByName(args[3]);
            if(target==null){
                message.reset("§c").addTranslation("cmd.frequencies.invalidPlayer", args[3]);
                message.sendTo(sender);
                return;
            }
            String uuid = target.getUniqueID().toString();
            String name = target.getName();

            if(args[1].equals("add")){
                if(ServerProxy.getConfig().isWhitelisted(target, frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.add.player.already", name, frequency);
                else {
                    if(ServerProxy.getConfig().addToRestricted(frequency, uuid, name)) message.reset("§2").addTranslation("cmd.frequencies.restricted.add.player.success", name, frequency);
                    else message.reset("§c").addTranslation("cmd.frequencies.restricted.add.player.failed", name, frequency);
                }
                message.sendTo(sender);
                return;
            }
            else if(args[1].equals("remove")){
                if(!ServerProxy.getConfig().isWhitelisted(target, frequency)) message.reset("§c").addTranslation("cmd.frequencies.restricted.remove.player.none", name, frequency);
                else {
                    if(ServerProxy.getConfig().removeFromRestricted(frequency, uuid, name)) message.reset("§2").addTranslation("cmd.frequencies.restricted.remove.player.success", name, frequency);
                    else message.reset("§c").addTranslation("cmd.frequencies.restricted.remove.player.failed", name, frequency);
                }
                message.sendTo(sender);
                return;
            }
        }
        sendMessage(sender, getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if(args.length==1) return getListOfStringsMatchingLastWord(args, this.commandArguments.keySet());
        else if(args.length==2){
            switch (args[0]) {
                case "blacklisted":
                case "restricted":
                    return getListOfStringsMatchingLastWord(args, this.frequenciesArguments);
                case "help":
                    return getListOfStringsMatchingLastWord(args, this.commandArguments.keySet());
            }
        }
        else if(args.length==4){
            if(args[0].equals("restricted") && (args[1].equals("add") || args[1].equals("remove"))) {
                String arg2 = args[2];
                if(arg2.matches("(\\d)*")) {
                    if(arg2.length() >= 1 && arg2.length() <= 4){
                        short f = Short.parseShort(arg2);
                        if(f>=1 && f<=1000) return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if(args.length==4){
            if(args[0].equals("restricted") && (args[1].equals("add") || args[1].equals("remove"))) {
                String arg2 = args[2];
                if(arg2.matches("(\\d)*")) {
                    if(arg2.length() >= 1 && arg2.length() <= 4){
                        short f = Short.parseShort(arg2);
                        return f >= 1 && f <= 1000;
                    }
                }
            }
        }
        return false;
    }

    public void sendMessage(ICommandSender sender, String message){
        sender.sendMessage(new TextComponentString(message));
    }
}