package fr.zeevoker2vex.radio.server;

import fr.nathanael2611.modularvoicechat.api.VoiceDispatchEvent;
import fr.nathanael2611.modularvoicechat.api.VoiceProperties;
import fr.nathanael2611.modularvoicechat.config.ServerConfig;
import fr.nathanael2611.modularvoicechat.util.Helpers;
import fr.zeevoker2vex.radio.common.CommonProxy;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.registry.ItemRegistry;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.List;

public class ServerProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @SubscribeEvent
    public void dispatchVoice(VoiceDispatchEvent event){
        EntityPlayer player = event.getSpeaker();
        if(RadioManager.isConnectOnRadio(player) && RadioManager.isSpeakingOn(player)){
            short frequency = RadioManager.getFrequency(player);

            List<EntityPlayer> connected = RadioManager.getPlayersConnectedOnFrequency(frequency);
            for(EntityPlayer target : connected){
                if(player==target) continue;
                event.setProperties(VoiceProperties.builder().with("isRadio", true).build());
                event.dispatchTo((EntityPlayerMP) target, 100, VoiceProperties.builder().with("isRadio", true).build());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event){
        EntityPlayer player = event.player;
        if(RadioManager.isConnectOnRadio(player)){
            // D'abord on éteint les radios, ensuite il arrête de parler, puis on le déconnecte.
            RadioItem.turnOffAllRadios(player);
            RadioManager.updatePlayerSpeaking(player, false);
            RadioManager.disconnectPlayerFromRadio(player);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event){
        if(event.getEntity() instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if(RadioManager.isConnectOnRadio(player)){
                // D'abord on éteint les radios, ensuite il arrête de parler, puis on le déconnecte.
                RadioItem.turnOffAllRadios(player);
                RadioManager.updatePlayerSpeaking(player, false);
                RadioManager.disconnectPlayerFromRadio(player);
            }
        }
    }

    @SubscribeEvent
    public void equipmentChange(LivingEquipmentChangeEvent event){
        if(event.getEntity() instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) event.getEntity();

            ItemStack from = event.getFrom();
            ItemStack to = event.getTo();

            // C'était une radio maintenant rien et l'itemstack n'est plus dans l'inventaire / Marche aussi pour le drop :shrug:
            boolean clearItem = RadioItem.isItemRadio(from) && to.getItem() == Items.AIR && !player.inventory.hasItemStack(from);

            // Si on change pour une autre radio que celle qu'on tenait.
            boolean changeRadio = RadioItem.areDifferentRadio(from, to);

            // Si tu te fais clear ou drop la radio, on te déconnecte et elle s'éteint
            if(clearItem){
                if(RadioManager.isConnectOnRadio(player)){
                    // D'abord on éteint la radio, ensuite il arrête de parler, puis on le déconnecte.
                    RadioItem.updateItemTag(from, false, Short.toString(RadioItem.getItemFrequency(from)));
                    RadioManager.updatePlayerSpeaking(player, false);
                    RadioManager.disconnectPlayerFromRadio(player);
                }
            }
            // Si tu changes d'item alors que tu avais une radio (on vérifie quand même que ce soit une radio en "from")
            if(changeRadio && RadioItem.isItemRadio(from)){
                // Si tu étais en train de parler
                if(RadioManager.isSpeakingOn(player)){
                    // Soit on change de radio et on arrêté de parler sur la fréquence actuelle, soit on a plus de fréquence donc "déconnexion".
                    RadioManager.playRadioSoundToFrequency(player, false);

                    // Si tu changes en fait pour une radio en parlant, ça te change de fréquence en éteignant l'autre radio
                    if(RadioItem.isItemRadio(to)){
                        RadioItem.updateItemTag(from, false, Short.toString(RadioItem.getItemFrequency(from)));
                        RadioManager.connectToFrequency(player, RadioItem.getItemFrequency(to));
                        RadioManager.playRadioSoundToFrequency(player, true);
                    }
                    // Si ce n'est pas pour une radio, alors tout simplement ça arrête la diffusion de ta voix
                    else {
                        // TODO SI ON N'A PAS LA RADIO EN MAIN MAIS QUE DANS LA CONFIG C'EST PAS OBLIGATOIRE, ON FAIT PAS
                        RadioManager.updatePlayerSpeaking(player, false);
                    }
                }
            }
            // Si tu changes pour une autre radio, que ce soit car ce n'est pas la même radio, ou car avant tu tenais un autre item et maintenant une radio, alors elle s'use.
            if(changeRadio && RadioItem.isItemRadio(to)){
                RadioItem.useRadio(to, AddonConfig.generalConfig.radioUse.heldDamage);
            }
        }
    }
}