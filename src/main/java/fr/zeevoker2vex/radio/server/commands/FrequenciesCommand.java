package fr.zeevoker2vex.radio.server.commands;

import fr.zeevoker2vex.radio.server.radio.RadioManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequenciesCommand extends CommandBase {

    @Override
    public String getName() {
        return "frequencies";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/frequencies";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        HashMap<EntityPlayer, Short> playerFrequency = RadioManager.playerFrequency;

        HashMap<Short, List<String>> frequencies = new HashMap<>();

        for(Map.Entry<EntityPlayer, Short> entry : playerFrequency.entrySet()){
            short frequency = entry.getValue();
            EntityPlayer player = entry.getKey();

            List<String> players = frequencies.getOrDefault(frequency, new ArrayList<>());
            players.add(player.getName());

            frequencies.put(frequency, players);
        }
        String message = "\n§cListe des fréquences utilisées par les joueurs :";
        for(Map.Entry<Short, List<String>> entry : frequencies.entrySet()){
            message += "\n§6"+entry.getKey()+"§eMhz §f=> ";

            List<String> playerNames = entry.getValue();
            for(String playerName : playerNames){
                message += "§7"+playerName+"§f, ";
            }
            message = message.substring(0, message.length()-2)+".";
        }

        if(frequencies.isEmpty()) message += "\n§6Aucune fréquence radio n'est utilisée par les joueurs.";

        message += "\n§c-----------------------\n";

        sender.sendMessage(new TextComponentString(message));
    }
}