package fr.zeevoker2vex.radio.server;

import fr.nathanael2611.modularvoicechat.api.VoiceDispatchEvent;
import fr.nathanael2611.modularvoicechat.api.VoiceProperties;
import fr.zeevoker2vex.radio.common.CommonProxy;
import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.client.RadioStatePacket;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import fr.zeevoker2vex.radio.server.config.FrequenciesConfig;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ServerProxy extends CommonProxy {

    private static FrequenciesConfig addonConfig;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        File configFolder = new File(event.getModConfigurationDirectory().getPath(), RadioAddon.CONFIG_FOLDER);
        if(!configFolder.exists()) configFolder.mkdirs();

        File configFile = new File(configFolder.getPath(), "RadioFrequencies.json");
        addonConfig = new FrequenciesConfig(configFile);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static FrequenciesConfig getConfig() {
        return addonConfig;
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @SubscribeEvent
    public void dispatchVoice(VoiceDispatchEvent event){
        EntityPlayer player = event.getSpeaker();
        // Si le joueur parle dans une radio = il est connect?? sur une fr??quence, et bien sur celle dont il tient la radio !
        if(RadioManager.isSpeakingOn(player)){
            ItemStack stack = player.getHeldItemMainhand();
            // On v??rifie 1?? Si c'est une radio 2?? Si elle est allum??e. Normalement si il parle c'est qu'elle est allum??e et donc qu'il est connect?? ?? une fr??quence !
            if(RadioItem.getRadioState(stack)){
                short frequency = RadioItem.getRadioFrequency(stack);
                List<EntityPlayer> connected = RadioManager.getPlayersConnectedOnFrequency(frequency);
                // Pour chaque joueur connect?? ?? cette fr??quence, on va r??cup??rer la radio dans son inventaire li?? ?? celle-ci. Par d??faut le volume est de 50.
                for(EntityPlayer target : connected){
                    if(player==target) continue;
                    short targetVolume = 50;
                    UUID targetRadioUUID = RadioManager.getPlayerRadioUUIDFromFrequency(target, frequency);
                    if(targetRadioUUID!=null) {
                        ItemStack targetStack = RadioItem.getRadioFromUUID(target, targetRadioUUID);
                        targetVolume = RadioItem.getRadioVolume(targetStack);
                    }
                    event.setProperties(VoiceProperties.builder().with("isRadio", true).build());
                    event.dispatchTo((EntityPlayerMP) target, targetVolume, VoiceProperties.builder().with("isRadio", true).build());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event){
        EntityPlayer player = event.player;
        // D'abord on ??teint les radios, ensuite il arr??te de parler, puis on le d??connecte.
        RadioItem.turnOffAllRadios(player);
        RadioManager.updatePlayerSpeaking(player, false, (short) 0);
        RadioManager.disconnectPlayerFromAll(player);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        // Des fois ??teindre au d??co ne marche pas
        RadioItem.turnOffAllRadios(event.player);
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event){
        EntityPlayer player = event.getEntityPlayer();
        // On force le joueur a arr??t?? de parler (
        RadioManager.updatePlayerSpeaking(player, false, (short) 0);
        // On fait le tour des drops, on ??teint les radios et d??connecte le joueur des fr??quences.
        for(EntityItem drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if(RadioItem.getRadioState(stack)){
                RadioItem.setRadioState(stack, false);
                NetworkHandler.getInstance().getNetwork().sendToAll(new RadioStatePacket(false, stack));
                RadioManager.disconnectPlayerFromFrequency(player, RadioItem.getRadioUUID(stack));
            }
        }
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event){
        if(event.getEntity() instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) event.getEntity();

            ItemStack from = event.getFrom();
            ItemStack to = event.getTo();

            // C'??tait une radio maintenant rien et l'itemstack n'est plus dans l'inventaire / Marche aussi pour le drop :shrug:
            boolean clearItem = RadioItem.isItemRadio(from) && !player.inventory.hasItemStack(from);
            // TODO Bug : quand on le switch en cliquant dessus puis en le reposant. Faudrait pouvoir get l'item qu'on tient ?? la souris

            // Si on change pour une autre radio que celle qu'on tenait.
            boolean changeRadio = RadioItem.areDifferentRadio(from, to);

            // Si tu te fais clear ou drop la radio, on te d??connecte, elle s'??teint et tu parles plus.
            if(clearItem){
                RadioItem.setRadioState(from, false);
                //System.out.println(RadioItem.getRadioState(from));
                NetworkHandler.getInstance().getNetwork().sendToAll(new RadioStatePacket(false, from));
                RadioManager.disconnectPlayerFromFrequency(player, RadioItem.getRadioUUID(from));
                RadioManager.updatePlayerSpeaking(player, false, (short) 0);
                //System.out.println("CLEAR/DROP");
                // TODO CLIENT STATE DOESN'T UPDATED
            }
            // Si tu changes d'item alors que tu avais une radio (on v??rifie quand m??me que ce soit une radio en "from")
            if(changeRadio && RadioItem.isItemRadio(from)){
                // Si tu ??tais en train de parler
                if(RadioManager.isSpeakingOn(player)){
                    // Tu fais arr??ter le joueur de parler, m??me si il change pour une autre radio il faut bien changer sa fr??quence o?? il parle.
                    RadioManager.updatePlayerSpeaking(player, false, (short) 0);

                    // Donc si tu changes pour une autre radio et qu'elle est allum??e (=tu es connect?? sur sa fr??quence), on te refait parler sur la fr??quence de cette radio.
                    if(RadioItem.getRadioState(to)) RadioManager.updatePlayerSpeaking(player, true, RadioItem.getRadioFrequency(to));
                }
            }
            // Si tu changes pour une autre radio, que ce soit car ce n'est pas la m??me radio, ou car avant tu tenais un autre item et maintenant une radio, alors elle s'use.
            if(changeRadio && RadioItem.isItemRadio(to)) RadioItem.useRadio(to, AddonConfig.generalConfig.radioUse.heldDamage);
        }
    }
}