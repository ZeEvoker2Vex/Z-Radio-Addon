package fr.zeevoker2vex.radio.server.radio;

import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.client.PlayRadioSoundPacket;
import fr.zeevoker2vex.radio.server.config.AddonConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.SERVER)
public class RadioManager {

    public static HashMap<EntityPlayer, HashMap<UUID, Short>> playersFrequencies = new HashMap<>();
    public static HashMap<EntityPlayer, Short> playersSpeaking = new HashMap<>();

    /**
     * @param player An EntityPlayer
     * @return If the player is connected on any frequency.
     */
    public static boolean isConnectOnAnyFrequency(EntityPlayer player){
        return playersFrequencies.containsKey(player);
    }

    /**
     * @param player An EntityPlayer
     * @param frequency A frequency
     * @return If the player is connected on the frequency
     */
    public static boolean isConnectOnFrequency(EntityPlayer player, short frequency){
        return isConnectOnAnyFrequency(player) && getFrequencies(player).contains(frequency);
    }

    /**
     * @param player An EntityPlayer
     * @return All frequencies on which the player is connected (only frequencies).
     */
    public static ArrayList<Short> getFrequencies(EntityPlayer player){
        return new ArrayList<>(getFrequenciesWithUUID(player).values());
    }

    /**
     * @param player An EntityPlayer
     * @return The map containing the UUIDs of radio ItemStack with their associated frequencies.
     */
    public static HashMap<UUID, Short> getFrequenciesWithUUID(EntityPlayer player){
        return playersFrequencies.getOrDefault(player, new HashMap<>());
    }

    /**
     * Disconnect the player from the frequency, removing it from the list and update the map value for the player. No change if player isn't connected on the frequency.
     * @param player An EntityPlayer
     * @param radioUUID The Radio UUID Tag associated to a frequency. Not using frequency because we don't know if we will disconnect the correct UUID else.
     */
    public static void disconnectPlayerFromFrequency(EntityPlayer player, UUID radioUUID){
        HashMap<UUID, Short> currentFrequencies = getFrequenciesWithUUID(player);
        // If the map is empty or has not the uuid as key.
        //System.out.println(radioUUID+"b/"+getFrequenciesWithUUID(player));
        if(currentFrequencies.isEmpty() || !currentFrequencies.containsKey(radioUUID)) return;
        currentFrequencies.remove(radioUUID);
        playersFrequencies.put(player, currentFrequencies);
        //System.out.println(player+"a/"+getFrequenciesWithUUID(player));
    }

    /**
     * Disconnect the player from all frequencies. Removing him from the map.
     * @param player An EntityPlayer
     */
    public static void disconnectPlayerFromAll(EntityPlayer player){
        playersFrequencies.remove(player);
    }

    /**
     * Connect the player to the frequency, adding it to the map with the UUID as key (and cleaning the list if only one frequency is allowed).
     * @param player An EntityPlayer
     * @param frequency A frequency
     * @param radioUUID The UUID of the radio ItemStack
     */
    public static void connectToFrequency(EntityPlayer player, short frequency, UUID radioUUID){
        if(frequency>=1 && frequency<=1000) {
            HashMap<UUID, Short> currentFrequencies = getFrequenciesWithUUID(player);
            if(!AddonConfig.generalConfig.canHearAllRadios) {
                currentFrequencies.clear();
                RadioItem.turnOffAllRadios(player);
                RadioItem.setRadioState(player.getHeldItemMainhand(), true);
            }
            currentFrequencies.put(radioUUID, frequency);
            playersFrequencies.put(player, currentFrequencies);
            //System.out.println(player.getName()+"/"+currentFrequencies);
        }
    }

    /**
     * @param frequency A frequency
     * @return All players connected on this frequency. Filter the map values and get the corresponding keys.
     */
    public static List<EntityPlayer> getPlayersConnectedOnFrequency(short frequency){
        return playersFrequencies.entrySet().stream().filter(entry -> entry.getValue().containsValue(frequency)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * WARNING : This method get the first UUID found /!\ If the player is connected with some radios on the same frequency, we can't know which is the good.
     * @param player An EntityPlayer
     * @param frequency A frequency
     * @return The UUID of the radio ItemStack linked to this frequency for the player. This is the Key from the map's value.
     */
    public static UUID getPlayerRadioUUIDFromFrequency(EntityPlayer player, short frequency){
        Optional<UUID> uuid = getFrequenciesWithUUID(player).entrySet().stream().filter(entry -> entry.getValue()==frequency).map(Map.Entry::getKey).findFirst();
        return uuid.orElse(null);
    }

    /**
     * Add or remove the player from the list of speaking players. Play a sound of radio to all players on the player frequency.
     * @param player An EntityPlayer
     * @param speaking True if the player is currently speaking, else false.
     * @param frequency Used when a player starts to speak. Else #getFrequencySpeaking is used to be sure of the frequency.
     */
    public static void updatePlayerSpeaking(EntityPlayer player, boolean speaking, short frequency){
        if((speaking && !isSpeakingOn(player) && isConnectOnFrequency(player, frequency)) || (!speaking && isSpeakingOn(player))){
            short usedFrequency = !speaking ? getFrequencySpeaking(player) : frequency;
            playRadioSoundToFrequency(speaking, usedFrequency);
            if(speaking) playersSpeaking.put(player, frequency);
            else playersSpeaking.remove(player);
        }
    }

    /**
     * @param player An EntityPlayer
     * @return True if the player is currently speaking, else false.
     */
    public static boolean isSpeakingOn(EntityPlayer player){
        return playersSpeaking.containsKey(player);
    }

    /**
     * @param player An EntityPlayer
     * @return The frequency on which the player speaks. 0 is no speaking.
     */
    public static short getFrequencySpeaking(EntityPlayer player){
        return playersSpeaking.getOrDefault(player, (short) 0);
    }

    /**
     * Play radio sound to all players on the player frequency.
     * @param speaking If the player is currently speaking
     * @param frequency A frequency
     */
    public static void playRadioSoundToFrequency(boolean speaking, short frequency){
        // Play sound even if the player isn't connected to the frequency, to notice others that he is disconnected for instance.
        getPlayersConnectedOnFrequency(frequency)
                .forEach(playerEntity -> NetworkHandler.getInstance().getNetwork().sendTo(new PlayRadioSoundPacket(speaking), (EntityPlayerMP) playerEntity));
    }
}