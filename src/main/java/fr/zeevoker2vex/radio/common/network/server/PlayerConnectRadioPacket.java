package fr.zeevoker2vex.radio.common.network.server;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.client.RadioResponsePacket;
import fr.zeevoker2vex.radio.server.ServerProxy;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class PlayerConnectRadioPacket implements IMessage {

    public String frequencyText;

    public PlayerConnectRadioPacket(String frequencyText) {
        this.frequencyText = frequencyText;
    }
    public PlayerConnectRadioPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.frequencyText = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.frequencyText);
    }

    public static class ServerHandler implements IMessageHandler<PlayerConnectRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PlayerConnectRadioPacket message, MessageContext ctx) {

            String frequencyText = message.frequencyText;
            EntityPlayer player = ctx.getServerHandler().player;

            ItemStack stack = player.getHeldItemMainhand();
            if(!RadioItem.isItemRadio(stack)) return null;

            boolean state = RadioItem.getRadioState(stack);

            short frequency = 0;
            if(frequencyText.matches("(\\d)*") && frequencyText.length()<5 && frequencyText.length()>0) frequency = Short.parseShort(frequencyText);
            if(frequency<1 || frequency>1000){
                return new RadioResponsePacket((short) -1, (short) -1, RadioResponsePacket.ResponseCode.CONNECT_INVALID_FREQUENCY, stack);
            }
            else {
                // Si la radio est correcte, que tu as la permission de te connecter à la fréquence, on te connecte (et déconnecte de l'ancienne fréquence si la radio était allumée).
                byte connectResponse = ServerProxy.getConfig().canPlayerConnect(player, frequency);
                if(connectResponse==1) return new RadioResponsePacket(frequency, (short) -1, RadioResponsePacket.ResponseCode.CONNECT_BLACKLISTED, stack);
                else if(connectResponse==2) return new RadioResponsePacket(frequency, (short) -1, RadioResponsePacket.ResponseCode.CONNECT_NO_PERM, stack);

                UUID radioUUID = RadioItem.getRadioUUID(stack);

                if(state) RadioManager.disconnectPlayerFromFrequency(player, radioUUID);
                RadioManager.connectToFrequency(player, frequency, radioUUID);
            }
            RadioItem.setRadioState(stack, true);
            RadioItem.setRadioFrequency(stack, frequency);
            return new RadioResponsePacket(frequency, (short) -1, RadioResponsePacket.ResponseCode.CONNECT_SUCCESS, stack);
        }
    }
}