package fr.zeevoker2vex.radio.common;

import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.registry.ItemRegistry;
import fr.zeevoker2vex.radio.common.registry.SoundRegistry;
import fr.zeevoker2vex.radio.server.commands.FrequenciesCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = RadioAddon.MOD_ID, name = RadioAddon.NAME, version = RadioAddon.VERSION)
public class RadioAddon {

    public static final String MOD_ID = "z-radio";
    public static final String NAME = "Radio Addon";
    public static final String VERSION = "1.1.2";

    public static final String CONFIG_FOLDER = "Z-MVC-Addons";

    @SidedProxy(clientSide = "fr.zeevoker2vex.radio.client.ClientProxy", serverSide = "fr.zeevoker2vex.radio.server.ServerProxy", modId = RadioAddon.MOD_ID)
    public static CommonProxy proxy;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
        MinecraftForge.EVENT_BUS.register(new SoundRegistry());

        NetworkHandler.getInstance().registerPackets();

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new FrequenciesCommand());
    }

    public static Logger getLogger() {
        return logger;
    }
}