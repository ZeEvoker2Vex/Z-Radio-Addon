package fr.zeevoker2vex.radio.server.config;

import fr.zeevoker2vex.radio.common.RadioAddon;
import net.minecraftforge.common.config.Config;

@Config(modid = RadioAddon.MOD_ID, name = RadioAddon.CONFIG_FOLDER + "/RadioAddon")
public class AddonConfig {

    @Config.Comment({"This is the general config of the Radio Addon"})
    public static General generalConfig = new General();

    public static class General {
        @Config.Comment("Category of radio use damage")
        public RadioUseDamage radioUse = new RadioUseDamage();

        @Config.Comment("Can players listen to all the radios they are connected to? false = hear only the last radio used.")
        public boolean canHearAllRadios = true;

        @Config.Comment("If true, op players can connect on all frequencies, even if they are restricted and even blacklisted.")
        public boolean opBypassRestrictions = true;
    }

    public static class RadioUseDamage {
        @Config.Comment("Is the radio damage activated")
        public boolean damageEnabled = false;

        @Config.Comment("How much damage holding a radio does. The radio has a durability of 300.")
        public int heldDamage = 10;

        @Config.Comment("How much damage talking on the radio does. The radio has a durability of 300.")
        public int speakDamage = 1;
    }
}