package fr.zeevoker2vex.radio.server.config;

import com.google.gson.*;
import fr.nathanael2611.modularvoicechat.util.Helpers;
import fr.zeevoker2vex.radio.common.utils.LogUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.SERVER)
public class FrequenciesConfig {

    /**
     * The list of all blacklisted frequencies (nobody can connect on them, else if opBypassRestrictions is true in .cfg config)
     */
    private final List<Short> blacklistedFrequencies = new ArrayList<>();

    /**
     * The map of all restricted frequencies and the players who can connect on them.
     * Key : the frequency Short
     * Value : a list of all players' info which is an array with the player name & player uuid (to work with crack or premium servers).
     */
    private final HashMap<Short, List<String[]>> restrictedFrequencies = new HashMap<>();

    /**
     * GSON : the gson instance to write json
     */
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    /**
     * The config file .json
     */
    private final File configFile;
    public FrequenciesConfig(File configFile) {
        this.configFile = configFile;
        // Create new file only if it doesn't exist, even if there is a problem into file.
        if(!configFile.exists() || configFile.length()==0) writeDefaultFile();
        else readConfigFile();
    }

    //region Config writing, reading & utils methods region
    /**
     * Create the config file and write only config keys.
     */
    public void writeDefaultFile(){
        try {
            LogUtils.basicLog("Config file doesn't exist, trying to create it..");
            this.configFile.createNewFile();

            JsonObject configJson = new JsonObject();
            // "blacklisted": []
            configJson.add("blacklisted", new JsonArray());
            // "restricted": {}
            configJson.add("restricted", new JsonObject());

            if(writeJson(configJson)) LogUtils.successLog("The default config file has been created and written!");
            else LogUtils.errorLog("An error occurred on writing default json in config file !");
        } catch (IOException e) {
            LogUtils.errorLog("An error occurred on trying to create config file.");
            e.printStackTrace();
        }
    }

    /**
     * Read the whole json file and fill blacklisted & restricted frequencies list and map.
     */
    public void readConfigFile(){
        LogUtils.basicLog("Starting to read config file..");
        if(!checkConfig()) return;
        JsonObject configJson = getConfigAsJsonObject();
        // "blacklisted": [1, 666, 999, 1000]
        JsonArray blacklisted = configJson.getAsJsonArray("blacklisted");
        for(JsonElement jsonElement : blacklisted){
            blacklistedFrequencies.add(jsonElement.getAsShort());
        }
        LogUtils.basicLog("End of reading all blacklisted frequencies : "+getBlacklistedFrequencies());

        /*
        "restricted": {
            "10": [
              {
                "uuid": "playerUUID",
                "name": "playerName"
              }
            ]
         }
         */
        JsonObject restricted = configJson.getAsJsonObject("restricted");
        for(Map.Entry<String, JsonElement> entry : restricted.entrySet()){
            String KEY = entry.getKey();
            LogUtils.basicLog("Reading "+KEY+" as restricted frequencies");

            // [{"uuid": "playerUUID","name": "playerName"},{"uuid": "playerUUID2","name": "playerName2"}]
            JsonArray whitelistedPlayers = entry.getValue().getAsJsonArray();

            List<String[]> players = new ArrayList<>();

            for(JsonElement playerElement : whitelistedPlayers){
                // {"uuid": "playerUUID","name": "playerName"}
                JsonObject playerObject = playerElement.getAsJsonObject();

                String uuid = playerObject.get("uuid").getAsString();
                String name = playerObject.get("name").getAsString();

                String[] playerInfo = new String[] {uuid, name};
                players.add(playerInfo);
            }
            restrictedFrequencies.put(Short.parseShort(KEY), players);
        }
        LogUtils.basicLog("End of reading all restricted frequencies.");
        LogUtils.successLog("The config file has been successfully read !");
    }

    /**
     * Write into the config file the jsonObject.
     * @param jsonObject A JsonObject
     */
    public boolean writeJson(JsonObject jsonObject){
        try {
            FileUtils.writeStringToFile(getConfigFile(), GSON.toJson(jsonObject), Charset.defaultCharset(), false);
            return true;
        } catch (IOException e) {
            LogUtils.errorLog("An error occurred on writing json in config file!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return If the configFile is valid : file exists, can be parsed to a valid json
     */
    private boolean isConfigValid() {
        return getConfigFile().exists() && getConfigFile().length()>0 && getConfigAsJsonObject() != null;
    }

    /**
     * @return The config as a Json Object. Return null if it can't parse.
     */
    private JsonObject getConfigAsJsonObject(){
        try {
            return (new JsonParser()).parse(Helpers.readFileToString(getConfigFile())).getAsJsonObject();
        }
        catch(JsonParseException exception){
            return null;
        }
    }

    /**
     * @return The config file used everywhere
     */
    public File getConfigFile(){
        return this.configFile;
    }

    /**
     * Check if the config is valid. Rewrite default file otherwise.
     * @return If the config is valid.
     */
    public boolean checkConfig(){
        if(!isConfigValid()){
            LogUtils.errorLog("Error when checking config, it appears that config isn't valid (not json syntax, empty file, or don't exist)\nTrying to delete & recreate default file.");
            configFile.delete();
            writeDefaultFile();
            return false;
        }
        return true;
    }
    //endregion

    //region Restricted & blacklisted frequencies region
    /**
     * @return The list of blacklisted frequencies
     */
    public List<Short> getBlacklistedFrequencies() {
        return blacklistedFrequencies;
    }

    /**
     * @param frequency A frequency
     * @return If the frequency is blacklisted (list contains it)
     */
    public boolean isBlacklisted(short frequency){
        return getBlacklistedFrequencies().contains(frequency);
    }

    /**
     * @return The map of restricted frequencies
     */
    public HashMap<Short, List<String[]>> getRestrictedFrequencies() {
        return restrictedFrequencies;
    }

    /**
     * @param frequency A frequency
     * @return If the frequency is restricted (map contains it as key)
     */
    public boolean isRestricted(short frequency){
        return getRestrictedFrequencies().containsKey(frequency);
    }

    /**
     * Check if player can connect to the frequency
     * @param player An EntityPlayer
     * @param frequency A frequency
     * @return A byte, 0 = can connect ; 1 = blacklisted ; 2 = restricted
     */
    public byte canPlayerConnect(EntityPlayer player, short frequency){
        boolean isOp = Arrays.stream(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayerNames()).anyMatch(name -> name.equals(player.getName()));
        boolean bypass = isOp && AddonConfig.generalConfig.opBypassRestrictions;

        // You are OP and you can bypass : connect
        if(bypass) return 0;

        // Blacklist is blacklist : 1
        if(isBlacklisted(frequency)) return 1;
        // Restricted and ?
        if(isRestricted(frequency)){
            // You are whitelisted : connect | Else : nop !
            if(isWhitelisted(player, frequency)) return 0;
            else return 2;
        }
        // Isn't blacklist, isn't restricted, you are not OP, but you can connect so !
        return 0;
    }

    /**
     * @param frequency A frequency
     * @return All players' info of this frequency.
     */
    public List<String[]> getPlayers(short frequency){
        return getRestrictedFrequencies().getOrDefault(frequency, new ArrayList<>());
    }

    /**
     * @param frequency A frequency
     * @return All players' name of this frequency.
     */
    public List<String> getPlayerNames(short frequency){
        return getPlayers(frequency).stream().map(info -> info[1]).collect(Collectors.toList());
    }

    /**
     * @param player An EntityPlayer
     * @param frequency A frequency
     * @return If the player is whitelisted for this frequency. == players' info contains his name or uuid.
     */
    public boolean isWhitelisted(EntityPlayer player, short frequency){
        String playerUUID = player.getUniqueID().toString();
        String playerName = player.getName();
        List<String[]> players = getPlayers(frequency);
        for(String[] playerInfo : players){
            String uuid = playerInfo[0];
            String name = playerInfo[1];
            if(playerUUID.equals(uuid) || playerName.equals(name)) return true;
        }
        return false;
    }
    //endregion

    //region Frequencies add/remove/setter + config saving region

    /**
     * Add it to the blacklisted frequencies : update in-code list and config property.
     * @param frequency A frequency
     * @return If no problem occurred = operation has worked
     */
    public boolean addToBlacklist(short frequency){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "blacklisted": [] and add an element (frequency) to it.
        JsonArray blacklisted = configJson.getAsJsonArray("blacklisted");
        blacklisted.add(frequency);
        // Update the "blacklisted" property.
        configJson.add("blacklisted", blacklisted);
        // Write into config file
        if(writeJson(configJson)) {
            this.blacklistedFrequencies.add(frequency);
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on adding a frequency to blacklist!");
            return false;
        }
    }
    /**
     * Remove it from the blacklisted frequencies : update in-code list and config property.
     * @param frequency A frequency
     * @return If no problem occurred = operation has worked
     */
    public boolean removeFromBlacklist(short frequency){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "blacklisted": [] and remove the element (frequency) to it.
        JsonArray blacklisted = configJson.getAsJsonArray("blacklisted");
        blacklisted.remove(new JsonPrimitive(frequency));
        // Update the "blacklisted" property.
        configJson.add("blacklisted", blacklisted);
        // Write into config file
        if(writeJson(configJson)) {
            this.blacklistedFrequencies.removeIf(freq -> freq==frequency);
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on removing a frequency from blacklist!");
            return false;
        }
    }

    /**
     * Add it to the restricted frequencies : update in-code list and config property.
     * @param frequency A frequency
     * @return If no problem occurred = operation has worked
     */
    public boolean addToRestricted(short frequency){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "restricted": {} and add an object (frequency) to it.
        JsonObject restricted = configJson.getAsJsonObject("restricted");
        restricted.add(String.valueOf(frequency), new JsonArray());
        // Update the "restricted" property.
        configJson.add("restricted", restricted);
        // Write into config file
        if(writeJson(configJson)) {
            this.restrictedFrequencies.put(frequency, new ArrayList<>());
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on adding a frequency to restricted!");
            return false;
        }
    }
    /**
     * Remove it from the restricted frequencies : update in-code list and config property.
     * @param frequency A frequency
     * @return If no problem occurred = operation has worked
     */
    public boolean removeFromRestricted(short frequency){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "restricted": {} and remove the object (frequency) to it.
        JsonObject restricted = configJson.getAsJsonObject("restricted");
        restricted.remove(String.valueOf(frequency));
        // Update the "restricted" property.
        configJson.add("restricted", restricted);
        // Write into config file
        if(writeJson(configJson)) {
            this.restrictedFrequencies.remove(frequency);
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on removing a frequency from restricted!");
            return false;
        }
    }

    /**
     * Add player's info to the restricted frequency : update in-code list and config property.
     * @param frequency A frequency
     * @param uuid An EntityPlayer UUID
     * @param name An EntityPlayer username
     * @return If no problem occurred = operation has worked
     */
    public boolean addToRestricted(short frequency, String uuid, String name){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "frequency": [] from "restricted": {}.
        JsonObject restricted = configJson.getAsJsonObject("restricted");
        JsonArray freqArray = restricted.getAsJsonArray(String.valueOf(frequency));
        // Create player object
        JsonObject playerObject = new JsonObject();
        playerObject.add("uuid", new JsonPrimitive(uuid));
        playerObject.add("name", new JsonPrimitive(name));
        // Add player object to frequency array
        freqArray.add(playerObject);
        // Add the new array to restricted object
        restricted.add(String.valueOf(frequency), freqArray);
        // Update the "restricted" property.
        configJson.add("restricted", restricted);
        // Write into config file
        if(writeJson(configJson)) {
            List<String[]> players = getPlayers(frequency);
            players.add(new String[]{uuid, name});
            this.restrictedFrequencies.put(frequency, players);
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on adding a frequency to restricted!");
            return false;
        }
    }
    /**
     * Remove player's info from the restricted frequency : update in-code list and config property.
     * @param frequency A frequency
     * @param uuid An EntityPlayer UUID
     * @param name An EntityPlayer username
     * @return If no problem occurred = operation has worked
     */
    public boolean removeFromRestricted(short frequency, String uuid, String name){
        if(!checkConfig()) return false;
        JsonObject configJson = getConfigAsJsonObject();
        // Get "frequency": [] from "restricted": {}.
        JsonObject restricted = configJson.getAsJsonObject("restricted");
        JsonArray freqArray = restricted.getAsJsonArray(String.valueOf(frequency));
        // Iterate all elements and verify if info match
        int i = 0;
        for (; i < freqArray.size(); i++) {
            JsonObject playerInfo = freqArray.get(i).getAsJsonObject();
            String uuidInfo = playerInfo.get("uuid").getAsString();
            String nameInfo = playerInfo.get("name").getAsString();

            if(uuidInfo.equals(uuid) || nameInfo.equals(name)) break;
        }
        // Remove the good object from the frequency array
        freqArray.remove(i);
        // Add the new array to restricted object
        restricted.add(String.valueOf(frequency), freqArray);
        // Update the "restricted" property.
        configJson.add("restricted", restricted);
        // Write into config file
        if(writeJson(configJson)) {
            List<String[]> players = getPlayers(frequency);
            players.removeIf(info -> info[0].equals(uuid) || info[1].equals(name));
            this.restrictedFrequencies.put(frequency, players);
            return true;
        }
        else {
            LogUtils.errorLog("An error occurred on adding a frequency to restricted!");
            return false;
        }
    }
    //endregion
}