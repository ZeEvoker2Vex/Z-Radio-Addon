package fr.zeevoker2vex.radio.common.network.server;

import fr.zeevoker2vex.radio.client.ClientProxy;
import fr.zeevoker2vex.radio.client.gui.RadioGui;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerConfigRadioPacket implements IMessage {

    public boolean state;
    public String frequencyText;

    public PlayerConfigRadioPacket(boolean state, String frequencyText) {
        this.state = state;
        this.frequencyText = frequencyText;
    }
    public PlayerConfigRadioPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.state = buf.readBoolean();
        this.frequencyText = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.state);
        ByteBufUtils.writeUTF8String(buf, this.frequencyText);
    }

    public static class ServerHandler implements IMessageHandler<PlayerConfigRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PlayerConfigRadioPacket message, MessageContext ctx) {

            boolean state = message.state;
            String frequencyText = message.frequencyText;
            EntityPlayer player = ctx.getServerHandler().player;

            short frequency = 0;
            if(frequencyText.matches("(\\d)*") && frequencyText.length()<5 && frequencyText.length()>0){
                frequency = Short.parseShort(frequencyText);
            }
            if(frequency<1 || frequency>1000){
                // si tu veux te deco, OK ça te déco et te renvoie la fréquence sur laquelle tu étais ou rien
                // si tu veux te co, soit tu es déjà co et ça te renvoie juste ta fréquence actuelle, soit ça te co pas et te renvoie 1
                if(!state && RadioManager.isConnectOnRadio(player)){
                    frequency = RadioManager.getFrequency(player);
                }
                else if(state){
                    if(RadioManager.isConnectOnRadio(player)) {
                        frequency = RadioManager.getFrequency(player);
                    }
                    else {
                        state = false;
                        frequency = 1;
                    }
                }
            }
            else {
                if(!state) RadioManager.disconnectPlayerFromRadio(player);
                else RadioManager.connectToFrequency(player, frequency);
            }
            RadioItem.updateItemTag(player.getHeldItemMainhand(), state, frequencyText);
            return new PlayerConfigRadioPacket(state, Short.toString(frequency));
        }
    }

    public static class ClientHandler implements IMessageHandler<PlayerConfigRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PlayerConfigRadioPacket message, MessageContext ctx) {

            Minecraft mc = Minecraft.getMinecraft();

            if(mc.currentScreen instanceof RadioGui){
                RadioGui radioGui = (RadioGui) mc.currentScreen;
                radioGui.frequencyField.setText(message.frequencyText);

                RadioItem.updateItemTag(radioGui.radio, message.state, message.frequencyText);

                // Seulement si le résultat est positif, alors on se connecte sur une nouvelle fréquence donc on modifie la dernière radio utilisée.
                if(message.state) ClientProxy.lastRadioUsed = radioGui.radio;
            }
            return null;
        }
    }
}