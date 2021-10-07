package fr.zeevoker2vex.radio.common.network.client;

import fr.zeevoker2vex.radio.common.registry.SoundRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayRadioSoundPacket implements IMessage {

    public boolean startSpeaking;

    public PlayRadioSoundPacket(boolean startSpeaking) {
        this.startSpeaking = startSpeaking;
    }
    public PlayRadioSoundPacket(){}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.startSpeaking = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.startSpeaking);
    }

    public static class ClientHandler implements IMessageHandler<PlayRadioSoundPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PlayRadioSoundPacket message, MessageContext ctx) {
            SoundEvent radioSound = message.startSpeaking ? SoundRegistry.SOUND_RADIO_ON : SoundRegistry.SOUND_RADIO_OFF;
            Minecraft.getMinecraft().player.playSound(radioSound, 100.0f, 1.0f);
            return null;
        }
    }
}