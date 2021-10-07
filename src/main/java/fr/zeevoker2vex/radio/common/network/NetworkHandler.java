package fr.zeevoker2vex.radio.common.network;

import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.network.client.PlayRadioSoundPacket;
import fr.zeevoker2vex.radio.common.network.client.RadioResponsePacket;
import fr.zeevoker2vex.radio.common.network.client.RadioStatePacket;
import fr.zeevoker2vex.radio.common.network.client.RadioUUIDPacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerDisconnectRadioPacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerSpeakingOnRadioPacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerConnectRadioPacket;
import fr.zeevoker2vex.radio.common.network.server.RadioChangeVolumePacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    private SimpleNetworkWrapper network;

    private static NetworkHandler instance;

    private int nextID = 0;

    public static NetworkHandler getInstance() {
        if (instance == null) instance = new NetworkHandler();
        return instance;
    }

    public SimpleNetworkWrapper getNetwork() {
        return network;
    }

    public void registerPackets() {
        this.network = NetworkRegistry.INSTANCE.newSimpleChannel(RadioAddon.MOD_ID);

        registerPacket(PlayRadioSoundPacket.ClientHandler.class, PlayRadioSoundPacket.class, Side.CLIENT);
        registerPacket(PlayerSpeakingOnRadioPacket.ClientHandler.class, PlayerSpeakingOnRadioPacket.class, Side.CLIENT);
        registerPacket(RadioResponsePacket.ClientHandler.class, RadioResponsePacket.class, Side.CLIENT);
        registerPacket(RadioStatePacket.ClientHandler.class, RadioStatePacket.class, Side.CLIENT);
        registerPacket(RadioUUIDPacket.ClientHandler.class, RadioUUIDPacket.class, Side.CLIENT);

        registerPacket(PlayerConnectRadioPacket.ServerHandler.class, PlayerConnectRadioPacket.class, Side.SERVER);
        registerPacket(PlayerDisconnectRadioPacket.ServerHandler.class, PlayerDisconnectRadioPacket.class, Side.SERVER);
        registerPacket(PlayerSpeakingOnRadioPacket.ServerHandler.class, PlayerSpeakingOnRadioPacket.class, Side.SERVER);
        registerPacket(RadioChangeVolumePacket.ServerHandler.class, RadioChangeVolumePacket.class, Side.SERVER);
    }

    private <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
        network.registerMessage(messageHandler, requestMessageType, nextID++, side);
    }
}