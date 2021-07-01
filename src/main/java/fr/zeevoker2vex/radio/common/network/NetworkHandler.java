package fr.zeevoker2vex.radio.common.network;

import fr.zeevoker2vex.radio.common.RadioAddon;
import fr.zeevoker2vex.radio.common.network.server.PlayerSpeakingOnRadioPacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerConfigRadioPacket;
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

        registerPacket(PlayerSpeakingOnRadioPacket.ServerHandler.class, PlayerSpeakingOnRadioPacket.class, Side.SERVER);
        registerPacket(PlayerSpeakingOnRadioPacket.ClientHandler.class, PlayerSpeakingOnRadioPacket.class, Side.CLIENT);
        registerPacket(PlayerConfigRadioPacket.ServerHandler.class, PlayerConfigRadioPacket.class, Side.SERVER);
        registerPacket(PlayerConfigRadioPacket.ClientHandler.class, PlayerConfigRadioPacket.class, Side.CLIENT);
    }

    private <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
        network.registerMessage(messageHandler, requestMessageType, nextID, side);
        nextID++;
    }
}