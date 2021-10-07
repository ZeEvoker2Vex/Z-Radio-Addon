package fr.zeevoker2vex.radio.common.network.server;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.client.RadioResponsePacket;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class PlayerDisconnectRadioPacket implements IMessage {

    public byte b;

    public PlayerDisconnectRadioPacket(byte b) {
        this.b = b;
    }
    public PlayerDisconnectRadioPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.b = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.b);
    }

    public static class ServerHandler implements IMessageHandler<PlayerDisconnectRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PlayerDisconnectRadioPacket message, MessageContext ctx) {

            EntityPlayer player = ctx.getServerHandler().player;

            ItemStack stack = player.getHeldItemMainhand();
            if(!RadioItem.isItemRadio(stack)) return null;

            boolean state = RadioItem.getRadioState(stack);

            // Si la radio est allumée, qu'il est bien connecté sur sa fréquence (normalement oui), alors on le déconnecte.
            // On laisse son ancienne fréquence, comme ça elle est déjà inscrite quand on reprend la radio.
            if(state){
                short frequency = RadioItem.getRadioFrequency(stack);
                if(RadioManager.isConnectOnFrequency(player, frequency)){
                    UUID radioUUID = RadioItem.getRadioUUID(stack);
                    RadioManager.disconnectPlayerFromFrequency(player, radioUUID);
                    RadioItem.setRadioState(stack, false);
                    return new RadioResponsePacket((short) -1, (short) -1, RadioResponsePacket.ResponseCode.DISCONNECT_SUCCESS, stack);
                }
            }
            return new RadioResponsePacket((short) -1, (short) -1, RadioResponsePacket.ResponseCode.DISCONNECT_ALREADY, stack);
        }
    }
}