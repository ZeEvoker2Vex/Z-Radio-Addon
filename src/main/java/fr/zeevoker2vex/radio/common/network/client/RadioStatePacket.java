package fr.zeevoker2vex.radio.common.network.client;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RadioStatePacket implements IMessage {

    public boolean state;
    public ItemStack stack;

    public RadioStatePacket(boolean state, ItemStack stack) {
        this.state = state;
        this.stack = stack;
    }
    public RadioStatePacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.state = buf.readBoolean();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.state);
        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    public static class ClientHandler implements IMessageHandler<RadioStatePacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(RadioStatePacket message, MessageContext ctx) {

            boolean state = message.state;
            ItemStack stack = message.stack;

            RadioItem.setRadioState(stack, state);
            //System.out.println(RadioItem.getRadioState(stack));
            return null;
        }
    }
}