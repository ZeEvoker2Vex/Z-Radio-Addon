package fr.zeevoker2vex.radio.server.config;

import fr.zeevoker2vex.radio.common.RadioAddon;
import net.minecraftforge.common.config.Config;

@Config(modid = RadioAddon.MOD_ID, name = RadioAddon.CONFIG_FOLDER + "/RadioAddon")
public class AddonConfig {

    @Config.Comment({"This is the general config of the Radio Addon"})
    public static General generalConfig = new General();

    public static class General {
        @Config.Comment("Radio use damage values")
        public RadioUseDamage radioUse = new RadioUseDamage();

    }

    public static class RadioUseDamage {
        @Config.Comment("Is radio use damage enabled ?")
        public boolean damageEnabled = false;

        @Config.Comment("How much damage held radio take")
        public int heldDamage = 10;

        @Config.Comment("How much damage speak on radio take")
        public int speakDamage = 1;
    }
}