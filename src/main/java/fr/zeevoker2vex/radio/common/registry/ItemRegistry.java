package fr.zeevoker2vex.radio.common.registry;

import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRegistry {

    public static final Item RADIO = new RadioItem();

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(RADIO);
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = RadioAddon.MOD_ID)
    public static class ModelRegistry {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void registerItemsModels(ModelRegistryEvent event) {
            registerModel(RADIO);
        }

        @SideOnly(Side.CLIENT)
        public static void registerModel(Item item) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}