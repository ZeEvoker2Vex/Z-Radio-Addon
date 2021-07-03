package fr.zeevoker2vex.radio.common.items;

import fr.zeevoker2vex.radio.client.ClientProxy;
import fr.zeevoker2vex.radio.client.gui.RadioGui;
import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.registry.ItemRegistry;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RadioItem extends Item {

    public static final String STATE_KEY_TAG = "state";
    public static final String FREQUENCY_KEY_TAG = "frequency";

    @SideOnly(Side.CLIENT)
    public static final String VOLUME_KEY_TAG = "volume";

    @SideOnly(Side.SERVER)
    public static final String UUID_KEY_TAG = "UUID";

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
            tooltip.add("Press Shift for more information");
            return;
        }
        tooltip.add("Right Click to use radio :");
        tooltip.add("- Turn on/off");
        tooltip.add("- Change frequency");
        tooltip.add("Press ["+ClientProxy.SPEAK_ON_RADIO.getDisplayName()+"] to speak on the radio");

        super.addInformation(stack, world, tooltip, flagIn);
    }

    /* TODO SI ON N'A PAS LA RADIO EN MAIN MAIS QUE DANS LA CONFIG C'EST PAS OBLIGATOIRE, ON FAIT PAS
        PB : si on tient pas la radio comment savoir si elle est ON
        PB2 : Méthode appelée 1 fois client, 1 fois serveur DONC server get config. Et client ? Sync par quel moyen ?
     */
    public static boolean isPlayerHeldActiveRadio(EntityPlayer player){
        ItemStack stack = player.getHeldItemMainhand();
        boolean heldRadio = isItemRadio(stack);
        if(heldRadio){
            if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey(STATE_KEY_TAG)) heldRadio = false;
            else if(!stack.getTagCompound().getBoolean(STATE_KEY_TAG)) heldRadio = false;
        }
        return heldRadio;
    }

    public static void updateItemTag(ItemStack stack, boolean state, String frequencyText){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        nbt.setBoolean(STATE_KEY_TAG, state);
        nbt.setShort(FREQUENCY_KEY_TAG, Short.parseShort(frequencyText));
    }

    public static short getItemFrequency(ItemStack stack){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        if(nbt.hasKey(RadioItem.FREQUENCY_KEY_TAG)) return nbt.getShort(RadioItem.FREQUENCY_KEY_TAG);

        return 0;
    }

    public static void turnOffAllRadios(EntityPlayer player){
        List<ItemStack> stacks = player.inventory.mainInventory.stream().filter(RadioItem::isItemRadio).collect(Collectors.toList());
        for(ItemStack stack : stacks){
            updateItemTag(stack, false, Short.toString(getItemFrequency(stack)));
        }
    }

    public static void useRadio(ItemStack stack, int damage){
        if(!AddonConfig.generalConfig.radioUse.damageEnabled) return;
        int currentDamage = stack.getItemDamage();
        stack.setItemDamage(currentDamage+damage);
    }

    @SideOnly(Side.CLIENT)
    public static void setRadioVolume(ItemStack stack, short volume){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        nbt.setShort(VOLUME_KEY_TAG, volume);
    }

    @SideOnly(Side.CLIENT)
    public static short getRadioVolume(ItemStack stack){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        if(nbt.hasKey(VOLUME_KEY_TAG)) return nbt.getShort(VOLUME_KEY_TAG);

        return 100;
    }

    @SideOnly(Side.SERVER)
    public static void initRadioUUID(ItemStack stack){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        nbt.setUniqueId(UUID_KEY_TAG, UUID.randomUUID());
    }

    @SideOnly(Side.SERVER)
    public static UUID getRadioUUID(ItemStack stack){
        NBTTagCompound nbt;
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        nbt = stack.getTagCompound();

        if(nbt.hasKey(UUID_KEY_TAG)) return nbt.getUniqueId(UUID_KEY_TAG);

        return null;
    }

    @SideOnly(Side.SERVER)
    public static boolean radioHasUUID(ItemStack stack){
        return getRadioUUID(stack)!=null;
    }

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

    public static boolean isItemRadio(ItemStack stack){
        return stack.getItem() == ItemRegistry.RADIO;
    }
}