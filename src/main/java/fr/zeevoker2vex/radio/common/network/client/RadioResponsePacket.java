package fr.zeevoker2vex.radio.common.network.client;

import fr.zeevoker2vex.radio.client.gui.RadioGui;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class RadioResponsePacket implements IMessage {

    public short frequency, volume;
    public ResponseCode responseCode;
    public ItemStack stack;

    public RadioResponsePacket(short frequency, short volume, ResponseCode responseCode, ItemStack stack) {
        this.frequency = frequency;
        this.volume = volume;
        this.responseCode = responseCode;
        this.stack = stack;
    }
    public RadioResponsePacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.frequency = buf.readShort();
        this.volume = buf.readShort();
        this.responseCode = ResponseCode.values()[buf.readInt()];
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.frequency);
        buf.writeShort(this.volume);
        buf.writeInt(this.responseCode.ordinal());
        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    public static class ClientHandler implements IMessageHandler<RadioResponsePacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(RadioResponsePacket message, MessageContext ctx) {

            ResponseCode responseCode = message.responseCode;
            short frequency = message.frequency, volume = message.volume;
            ItemStack stack = message.stack;

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            boolean isOwnStack = RadioItem.areDifferentRadio(stack, player.getHeldItemMainhand());

            if(isOwnStack) {
                switch(responseCode){
                    case CONNECT_SUCCESS:
                        RadioItem.setRadioFrequency(stack, frequency);
                        RadioItem.setRadioState(stack, true);
                        break;
                    case CONNECT_NO_PERM:
                    case CONNECT_BLACKLISTED:
                        RadioItem.setRadioFrequency(stack, frequency); break;
                    case DISCONNECT_SUCCESS:
                        RadioItem.setRadioState(stack, false); break;
                    case VOLUME_SUCCESS:
                    case VOLUME_INVALID:
                        RadioItem.setRadioVolume(stack, volume); break;

                    default:
                        break;
                }
                if(mc.currentScreen instanceof RadioGui){
                    RadioGui radioGui = (RadioGui) mc.currentScreen;

                    if(frequency>0) radioGui.frequencyField.setText(Short.toString(frequency));

                    radioGui.showResponse(responseCode);
                }
            }
            return null;
        }
    }

    public enum ResponseCode {

        CONNECT_SUCCESS("radio.responseCode.connect.success", new Color(40, 119, 11).getRGB()), // Change frequency and state
        CONNECT_INVALID_FREQUENCY("radio.responseCode.connect.invalidFrequency", new Color(159, 6, 6).getRGB()), // Do nothing
        CONNECT_NO_PERM("radio.responseCode.connect.noPerm", Color.RED.getRGB()), // Change frequency but not state
        CONNECT_BLACKLISTED("radio.responseCode.connect.blacklisted",Color.RED.getRGB() ), // Change frequency

        DISCONNECT_SUCCESS("radio.responseCode.disconnect.success", new Color(16, 154, 6).getRGB()), // Change state
        DISCONNECT_ALREADY("radio.responseCode.disconnect.already", new Color(227, 127, 13).getRGB()), // Do nothing

        VOLUME_SUCCESS("radio.responseCode.volume.success", new Color(40, 119, 11).getRGB()), // Change volume
        VOLUME_INVALID("radio.responseCode.volume.invalid", new Color(159, 6, 6).getRGB()); // Do nothing

        public String unlocalizedText;
        public int textColor;

        /**
         * The constructor of ResponseCode
         * @param unlocalizedText The unlocalized code of the text
         * @param textColor The color of the text
         */
        ResponseCode(String unlocalizedText, int textColor) {
            this.unlocalizedText = unlocalizedText;
            this.textColor = textColor;
        }

        public String getUnlocalizedText() {
            return unlocalizedText;
        }

        public int getTextColor() {
            return textColor;
        }
    }
}