package fr.zeevoker2vex.radio.common.network.server;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.registry.SoundRegistry;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import fr.zeevoker2vex.radio.server.radio.RadioManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerSpeakingOnRadioPacket implements IMessage {

    public boolean startSpeaking;

    public PlayerSpeakingOnRadioPacket(boolean startSpeaking) {
        this.startSpeaking = startSpeaking;
    }
    public PlayerSpeakingOnRadioPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.startSpeaking = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.startSpeaking);
    }

    public static class ServerHandler implements IMessageHandler<PlayerSpeakingOnRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PlayerSpeakingOnRadioPacket message, MessageContext ctx) {

            EntityPlayer player = ctx.getServerHandler().player;

            // TODO SI ON N'A PAS LA RADIO EN MAIN MAIS QUE DANS LA CONFIG C'EST PAS OBLIGATOIRE, ON FAIT PAS | DIRECT DANS LA METHODE
            boolean heldRadio = RadioItem.isPlayerHeldActiveRadio(player);
            if(heldRadio) RadioManager.connectToFrequency(player, RadioItem.getItemFrequency(player.getHeldItemMainhand()));

            boolean speaking = heldRadio && message.startSpeaking;
            RadioManager.updatePlayerSpeaking(player, speaking);

            if(speaking) {
                RadioItem.useRadio(player.getHeldItemMainhand(), AddonConfig.generalConfig.radioUse.speakDamage);
            }
            return null;
        }
    }

    public static class ClientHandler implements IMessageHandler<PlayerSpeakingOnRadioPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PlayerSpeakingOnRadioPacket message, MessageContext ctx) {

            SoundEvent radioSound = message.startSpeaking ? SoundRegistry.SOUND_RADIO_ON : SoundRegistry.SOUND_RADIO_OFF;
            Minecraft.getMinecraft().player.playSound(radioSound, 100.0f, 1.0f);
            return null;
        }
    }
}