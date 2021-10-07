package fr.zeevoker2vex.radio.common.items;

import fr.zeevoker2vex.radio.client.ClientProxy;
import fr.zeevoker2vex.radio.client.gui.RadioGui;
import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.client.RadioUUIDPacket;
import fr.zeevoker2vex.radio.common.registry.ItemRegistry;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RadioItem extends Item {

    public static final String STATE_KEY_TAG = "state";
    public static final String FREQUENCY_KEY_TAG = "frequency";
    public static final String UUID_KEY_TAG = "RadioUUID";
    public static final String VOLUME_KEY_TAG = "volume";

    public RadioItem() {
        setRegistryName(RadioAddon.MOD_ID, "radio");
        setUnlocalizedName(RadioAddon.MOD_ID+".radio");

        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.MISC);
        setMaxDamage(300);

        addPropertyOverride(new ResourceLocation(RadioAddon.MOD_ID, "radioState"), (stack, worldIn, entityIn) -> {
            if(stack.hasTagCompound()){
                if(stack.getTagCompound().hasKey(STATE_KEY_TAG)){
                    // state "true" : on | else off
                    return stack.getTagCompound().getBoolean(STATE_KEY_TAG) ? 1.0F : 0.0F;
                }
            }
            // Aucun tag (soit AUCUN TAG, soit pas le tag "state") : éteint
            return 0.0F;
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        Minecraft.getMinecraft().displayGuiScreen(new RadioGui(stack));
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
        if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(I18n.format("radio.tooltip.frequency", getRadioFrequency(stack)));
            tooltip.add(I18n.format("radio.tooltip.volume", getRadioVolume(stack)));
            tooltip.add(I18n.format("radio.tooltip.state", getRadioState(stack) ? "ON" : "OFF"));
            tooltip.add("");
            tooltip.add(I18n.format("radio.tooltip.shift"));
        }
        else {
            tooltip.add(I18n.format("radio.tooltip.info.title"));
            tooltip.add(I18n.format("radio.tooltip.info.state"));
            tooltip.add(I18n.format("radio.tooltip.info.frequency"));
            tooltip.add(I18n.format("radio.tooltip.info.volume"));
            tooltip.add("");
            tooltip.add(I18n.format("radio.tooltip.info.key", ClientProxy.SPEAK_ON_RADIO.getDisplayName()));
        }
        super.addInformation(stack, world, tooltip, flagIn);
    }

    /*
     * Utils functions
     */

    /**
     * Get the NBTTagCompound of the stack, or init it if it doesn't have.
     * @param stack An ItemStack
     * @return The NBTTagCompound of the stack.
     */
    public static NBTTagCompound getTagCompound(ItemStack stack){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();
        return nbt;
    }
    /**
     * @param stack An ItemStack
     * @return True if the stack is a RADIO item.
     */
    public static boolean isItemRadio(ItemStack stack){
        return stack.getItem() == ItemRegistry.RADIO;
    }

    /**
     * @param player An EntityPlayer
     * @return True if the player held a radio that is on.
     */
    public static boolean isPlayerHeldActiveRadio(EntityPlayer player){
        return getRadioState(player.getHeldItemMainhand());
    }

    /**
     * Change the state of the radio stack.
     * @param stack A radio ItemStack
     * @param state The state of radio (on/off=true/false)
     */
    public static void setRadioState(ItemStack stack, boolean state){
        if(!isItemRadio(stack)) return;
        NBTTagCompound nbt = getTagCompound(stack);
        nbt.setBoolean(STATE_KEY_TAG, state);
    }
    /**
     * Change the Frequency of the radio stack.
     * @param stack A radio ItemStack
     * @param frequencyText The frequency as String
     */
    public static void setRadioFrequency(ItemStack stack, short frequencyText){
        if(!isItemRadio(stack)) return;
        NBTTagCompound nbt = getTagCompound(stack);
        nbt.setShort(FREQUENCY_KEY_TAG, frequencyText);
    }
    /**
     * Change the Volume of the radio stack
     * @param stack A radio ItemStack
     * @param volume The volume of the radio
     */
    public static void setRadioVolume(ItemStack stack, short volume){
        if(!isItemRadio(stack)) return;
        NBTTagCompound nbt = getTagCompound(stack);
        nbt.setShort(VOLUME_KEY_TAG, volume);
    }

    /**
     * @param stack A radio ItemStack
     * @return The state of the radio. False if isn't radio or if it hasn't state tag.
     */
    public static boolean getRadioState(ItemStack stack){
        if(isItemRadio(stack)) {
            NBTTagCompound nbt = getTagCompound(stack);
            if(nbt.hasKey(STATE_KEY_TAG)) return nbt.getBoolean(STATE_KEY_TAG);
        }
        return false;
    }
    /**
     * @param stack A radio ItemStack
     * @return The frequency of the radio. 0 if isn't radio or if it hasn't frequency tag.
     */
    public static short getRadioFrequency(ItemStack stack){
        if(isItemRadio(stack)) {
            NBTTagCompound nbt = getTagCompound(stack);
            if(nbt.hasKey(FREQUENCY_KEY_TAG)) return nbt.getShort(FREQUENCY_KEY_TAG);
        }
        return 0;
    }
    /**
     * @param stack A radio ItemStack
     * @return The volume of the radio. 100% if isn't radio or if it hasn't volume tag.
     */
    public static short getRadioVolume(ItemStack stack){
        if(isItemRadio(stack)) {
            NBTTagCompound nbt = getTagCompound(stack);
            if(nbt.hasKey(VOLUME_KEY_TAG)) return nbt.getShort(VOLUME_KEY_TAG);
        }
        return 100;
    }

    /**
     * Turn off all radios in the inventory of the player.
     * @param player A EntityPlayer
     */
    public static void turnOffAllRadios(EntityPlayer player){
        List<ItemStack> stacks = player.inventory.mainInventory.stream().filter(RadioItem::getRadioState).collect(Collectors.toList());
        stacks.forEach(stack -> setRadioState(stack, false));
    }

    /**
     * Damage the stack.
     * @param stack A radio ItemStack
     * @param damage A damage value
     */
    public static void useRadio(ItemStack stack, int damage){
        if(!AddonConfig.generalConfig.radioUse.damageEnabled || !isItemRadio(stack)) return;
        int currentDamage = stack.getItemDamage();
        stack.setItemDamage(currentDamage+damage);
    }

    /*
     * Radio UUID part
     */

    /**
     * Set the UUID tag of the tag randomly.
     * @param stack A Radio ItemStack
     * @return The new UUID
     */
    public static UUID initRadioUUID(ItemStack stack){
        if(FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
            if (!isItemRadio(stack)) return null;
            UUID uuid = UUID.randomUUID();
            setRadioUUID(stack, uuid);
            NetworkHandler.getInstance().getNetwork().sendToAll(new RadioUUIDPacket(uuid, stack));
            return uuid;
        }
        return null;
    }

    /**
     * Change the UUID of the radio stack.
     * @param stack A Radio ItemStack
     * @param uuid The UUID of the radio
     */
    public static void setRadioUUID(ItemStack stack, UUID uuid){
        if(!isItemRadio(stack)) return;
        NBTTagCompound nbt = getTagCompound(stack);
        nbt.setString(UUID_KEY_TAG, uuid.toString());
    }

    /**
     * @param stack A radio ItemStack
     * @return The UUID tag of the radio, or null.
     */
    public static UUID getRadioUUID(ItemStack stack){
        if(isItemRadio(stack)) {
            NBTTagCompound nbt = getTagCompound(stack);
            if(nbt.hasKey(UUID_KEY_TAG)) return UUID.fromString(nbt.getString(UUID_KEY_TAG));
            else return initRadioUUID(stack);
        }
        return null;
    }

    /**
     * @param player An EntityPlayer
     * @param uuid A Radio UUID tag
     * @return The Radio ItemStack in the player inventory that has this UUID tag.
     */
    public static ItemStack getRadioFromUUID(EntityPlayer player, UUID uuid){
        Optional<ItemStack> radioStack = player.inventory.mainInventory.stream().filter(stack -> Objects.equals(getRadioUUID(stack), uuid)).findFirst();
        return radioStack.orElse(ItemStack.EMPTY);
    }

    /**
     * @param stack A Radio ItemStack
     * @return If the stack has uuid tag.
     */
    public static boolean radioHasUUID(ItemStack stack){
        if(isItemRadio(stack)) {
            NBTTagCompound nbt = getTagCompound(stack);
            return nbt.hasKey(UUID_KEY_TAG);
        }
        return false;
    }

    /**
     * @param stack1 The first ItemStack
     * @param stack2 The second ItemStack
     * @return If the two stacks are different radios. True if one isn't a radio, or if one hasn't uuid tag, or if the two uuid tags are different.
     */
    public static boolean areDifferentRadio(ItemStack stack1, ItemStack stack2){
        if(!isItemRadio(stack1) || !isItemRadio(stack2)) return true;
        if(!radioHasUUID(stack1)){
            initRadioUUID(stack1);
            return true;
        }
        else if(!radioHasUUID(stack2)){
            initRadioUUID(stack2);
            return true;
        }
        else return getRadioUUID(stack1) != getRadioUUID(stack2);
    }
}