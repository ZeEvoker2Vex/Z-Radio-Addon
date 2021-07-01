package fr.zeevoker2vex.radio.common.registry;

import fr.zeevoker2vex.radio.common.RadioAddon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoundRegistry {

    private static final ResourceLocation RL_SOUND_RADIO_ON = new ResourceLocation(RadioAddon.MOD_ID, "radio_on");
    private static final ResourceLocation RL_SOUND_RADIO_OFF = new ResourceLocation(RadioAddon.MOD_ID, "radio_off");

    public static final SoundEvent SOUND_RADIO_ON = new SoundEvent(RL_SOUND_RADIO_ON);
    public static final SoundEvent SOUND_RADIO_OFF = new SoundEvent(RL_SOUND_RADIO_OFF);

    @SubscribeEvent
    public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {

        SOUND_RADIO_ON.setRegistryName(RadioAddon.MOD_ID, "sound.radio_on");
        SOUND_RADIO_OFF.setRegistryName(RadioAddon.MOD_ID, "sound.radio_off");

        event.getRegistry().registerAll(SOUND_RADIO_ON, SOUND_RADIO_OFF);
    }
}