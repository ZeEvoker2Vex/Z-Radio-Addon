package fr.zeevoker2vex.radio.common.network.server;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.client.RadioResponsePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RadioChangeVolumePacket implements IMessage {

    public short volume;

    public RadioChangeVolumePacket(short volume) {
        this.volume = volume;
    }
    public RadioChangeVolumePacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.volume = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.volume);
    }

    public static class ServerHandler implements IMessageHandler<RadioChangeVolumePacket, IMessage> {
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(RadioChangeVolumePacket message, MessageContext ctx) {

            short volume = message.volume;
            EntityPlayer player = ctx.getServerHandler().player;

            ItemStack stack = player.getHeldItemMainhand();
            if(!RadioItem.isItemRadio(stack)) return null;

            if(volume<0 || volume>100){
                volume = (short) (volume<0 ? 0 : 100);
                return new RadioResponsePacket((short) -1, volume, RadioResponsePacket.ResponseCode.VOLUME_INVALID, stack);
            }
            RadioItem.setRadioVolume(stack, volume);
            return new RadioResponsePacket((short) -1, volume, RadioResponsePacket.ResponseCode.VOLUME_SUCCESS, stack);
        }
    }
}