package fr.zeevoker2vex.radio.client;

import fr.nathanael2611.modularvoicechat.api.VoiceKeyEvent;
import fr.nathanael2611.modularvoicechat.api.VoicePlayEvent;
import fr.nathanael2611.modularvoicechat.client.gui.GuiConfig;
import fr.nathanael2611.modularvoicechat.client.voice.audio.MicroManager;
import fr.zeevoker2vex.radio.common.CommonProxy;
import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.server.PlayerSpeakingOnRadioPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding SPEAK_ON_RADIO = new KeyBinding("key." + RadioAddon.MOD_ID + ".speakonradio", Keyboard.KEY_X, "key.categories." + RadioAddon.MOD_ID);

    private boolean speaking = false;
    private Minecraft mc;
    public static ItemStack lastRadioUsed = ItemStack.EMPTY;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        mc = Minecraft.getMinecraft();

        ClientRegistry.registerKeyBinding(SPEAK_ON_RADIO);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @SubscribeEvent
    public void onVoiceKey(VoiceKeyEvent event) {
        if (SPEAK_ON_RADIO.isKeyDown()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (!GuiConfig.audioTesting) {
                ItemStack current = mc.player==null ? ItemStack.EMPTY : mc.player.getHeldItemMainhand();
                // TODO SI ON N'A PAS LA RADIO EN MAIN MAIS QUE DANS LA CONFIG C'EST PAS OBLIGATOIRE, ON FAIT PAS | DIRECT DANS LA METHODE
                if (GameSettings.isKeyDown(SPEAK_ON_RADIO) && RadioItem.isPlayerHeldActiveRadio(mc.player)) {
                    if (MicroManager.isRunning() && !MicroManager.getHandler().isSending()) {
                        if (!this.speaking) {
                            NetworkHandler.getInstance().getNetwork().sendToServer(new PlayerSpeakingOnRadioPacket(true));
                            this.speaking = true;
                        }
                        // Si on prend une radio ou change pour une autre tout en parlant, on change de fréquence alors on modifie la dernière radio
                        if(RadioItem.areDifferentRadio(lastRadioUsed, current) && RadioItem.isItemRadio(current)) lastRadioUsed = current;

                        MicroManager.getHandler().start();
                    }
                } else {
                    if (MicroManager.isRunning() && MicroManager.getHandler().isSending()) {
                        if (this.speaking) {
                            NetworkHandler.getInstance().getNetwork().sendToServer(new PlayerSpeakingOnRadioPacket(false));
                            this.speaking = false;

                            // Si on a arrêté de parler (si on change de main ça coupe notre voix mais plus de radio en main donc on garde l'autre)
                            if(RadioItem.areDifferentRadio(lastRadioUsed, current) && RadioItem.isItemRadio(current)) lastRadioUsed = current;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onVoicePlay(VoicePlayEvent event){
        if(event.getProperties().getBooleanValue("isRadio")) event.setVolumePercent(RadioItem.getRadioVolume(lastRadioUsed));
    }
}