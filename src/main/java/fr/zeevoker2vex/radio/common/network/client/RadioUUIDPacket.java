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

import java.util.UUID;

public class RadioUUIDPacket implements IMessage {

    public UUID uuid;
    public ItemStack stack;

    public RadioUUIDPacket(UUID uuid, ItemStack stack) {
        this.uuid = uuid;
        this.stack = stack;
    }
    public RadioUUIDPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    public static class ClientHandler implements IMessageHandler<RadioUUIDPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(RadioUUIDPacket message, MessageContext ctx) {

            UUID uuid = message.uuid;
            ItemStack stack = message.stack;

            RadioItem.setRadioUUID(stack, uuid);
            return null;
        }
    }
}