package fr.zeevoker2vex.radio.common.utils;

import fr.zeevoker2vex.radio.common.RadioAddon;

public class LogUtils {

    public static void successLog(String message){
        RadioAddon.getLogger().info("§2"+message);
    }

    public static void errorLog(String message){
        RadioAddon.getLogger().error(message);
    }

    public static void basicLog(String message){
        RadioAddon.getLogger().info("§7"+message);
    }

    public static void warnLog(String message){
        RadioAddon.getLogger().info("§6"+message);
    }
}