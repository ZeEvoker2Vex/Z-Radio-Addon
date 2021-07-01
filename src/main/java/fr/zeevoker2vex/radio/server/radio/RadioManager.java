package fr.zeevoker2vex.radio.server.radio;

import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.server.PlayerSpeakingOnRadioPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SideOnly(Side.SERVER)
public class RadioManager {

    public static HashMap<EntityPlayer, Short> playerFrequency = new HashMap<>();
    public static List<EntityPlayer> playersSpeaking = new ArrayList<>();

    public static boolean isConnectOnRadio(EntityPlayer player){
        return playerFrequency.containsKey(player);
    }

    public static short getFrequency(EntityPlayer player){
        return playerFrequency.get(player);
    }

    public static void disconnectPlayerFromRadio(EntityPlayer player){
        playerFrequency.remove(player);
    }

    public static void connectToFrequency(EntityPlayer player, short frequency){
        if(frequency>=1 && frequency<=1000) {
            playerFrequency.put(player, frequency);

        }
    }

    public static List<EntityPlayer> getPlayersConnectedOnFrequency(short frequency){
        List<Map.Entry<EntityPlayer, Short>> entries = playerFrequency.entrySet().stream().filter(entry -> entry.getValue()==frequency).collect(Collectors.toList());
        List<EntityPlayer> connectedPlayers = new ArrayList<>();
        for(Map.Entry<EntityPlayer, Short> entry : entries){
            connectedPlayers.add(entry.getKey());
        }
        return connectedPlayers;
    }

    public static void updatePlayerSpeaking(EntityPlayer player, boolean speaking){
        // Si il parle et qu'il n'était pas en train de parler OU si il s'arrête de parler alors qu'il parlait : playsound
        if((speaking && !isSpeakingOn(player)) || (!speaking && isSpeakingOn(player))) playRadioSoundToFrequency(player, speaking);

        if(speaking) playersSpeaking.add(player);
        else playersSpeaking.remove(player);
    }

    public static boolean isSpeakingOn(EntityPlayer player){
        return playersSpeaking.contains(player);
    }

    public static void playRadioSoundToFrequency(EntityPlayer player, boolean speaking){
        getPlayersConnectedOnFrequency(getFrequency(player))
                .forEach(playerEntity -> NetworkHandler.getInstance().getNetwork().sendTo(new PlayerSpeakingOnRadioPacket(speaking), (EntityPlayerMP) playerEntity));
    }
}