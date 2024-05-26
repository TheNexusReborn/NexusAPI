package com.thenexusreborn.api.gamearchive;

import com.google.gson.*;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starlib.time.TimeFormat;
import com.thenexusreborn.api.NexusAPI;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class GameLogExporter {
    
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/{name}";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = NexusAPI.getApi().getLogger();
    private static final Map<String, UUID> nameToUUID = new HashMap<>();
    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss:SSS";
    private static final TimeFormat TIME_FORMAT = new TimeFormat("%*#0h% %*#0m% %*#0s% %*#0ms%");
    
    private File baseDir;

    public GameLogExporter(File baseDir) {
        this.baseDir = baseDir;
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    public void exportGames() throws SQLException, IOException {
        //LOGGER.info("Starting game export.");

        //LOGGER.info("Retrieving games from database...");
        List<GameInfo> gameInfos = NexusAPI.getApi().getPrimaryDatabase().get(GameInfo.class);
        //LOGGER.info("Retrieved " + gameInfos.size() + " games from database.");

        for (GameInfo gameInfo : gameInfos) {
            exportGameInfo(gameInfo);
        }
    }
    
    public JsonObject getGameJson(int gameId) throws FileNotFoundException {
        File jsonFile = new File(baseDir, gameId + File.separator + gameId + ".json");
        if (!jsonFile.exists()) {
            return null;
        }

        return new JsonParser().parse(new FileReader(jsonFile)).getAsJsonObject();
    }
    
    public List<String> getGameTxt(int gameId) throws IOException {
        File txtFile = new File(baseDir, gameId + File.separator + gameId + ".txt");
        if (!txtFile.exists()) {
            return null;
        }
        
        List<String> gameText = new LinkedList<>();
        
        try (FileReader fr = new FileReader(txtFile); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                gameText.add(line);
            }
        }
        
        return gameText;
    }
    
    public void exportGameInfo(GameInfo gameInfo) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        
        File gameDir = new File(baseDir, gameInfo.getId() + "");
        if (!gameDir.exists()) {
            gameDir.mkdirs();
        } else {
            //LOGGER.info("Game " + gameInfo.getId() + " has already been exported.");
            return;
        }

        //LOGGER.info("Processing Game with ID " + gameInfo.getId() + " ...");
        JsonObject gameJson = new JsonObject();
        List<String> gameTxt = new LinkedList<>();

        gameTxt.add("----Basic Game Info----");

        gameJson.addProperty("id", gameInfo.getId());
        gameTxt.add("Game ID: " + gameInfo.getId());

        gameJson.addProperty("start", gameInfo.getGameStart());
        gameTxt.add("Start: " + dateFormat.format(gameInfo.getGameStart()));

        gameJson.addProperty("end", gameInfo.getGameEnd());
        gameTxt.add("End: " + dateFormat.format(gameInfo.getGameEnd()));

        gameJson.addProperty("server", gameInfo.getServerName());
        gameTxt.add("Server Name: " + gameInfo.getServerName());

        gameJson.addProperty("map", gameInfo.getMapName());
        gameTxt.add("Map Name: " + gameInfo.getMapName().replace("''", "'"));

        gameJson.addProperty("playercount", gameInfo.getPlayerCount());
        gameTxt.add("Player Count: " + gameInfo.getPlayerCount());

        gameJson.addProperty("length", gameInfo.getLength());
        gameTxt.add("Length: " + TIME_FORMAT.format(gameInfo.getLength()));

        JsonObject playersObject = new JsonObject();
        StringBuilder playersTxtBuilder = new StringBuilder();

        //LOGGER.info("Processing Players for Game " + gameInfo.getId());
        for (String playerName : gameInfo.getPlayers()) {
            //LOGGER.info("Processing Player " + playerName + " for game " + gameInfo.getId());

            UUID uuid = getUUID(playerName);

            if (uuid == null) {
                playersObject.addProperty(playerName, "Could not get UUID.");
            } else {
                playersObject.addProperty(playerName, uuid.toString());
            }

            playersTxtBuilder.append(playerName).append(",");
        }

        gameJson.add("players", playersObject);
        gameTxt.add("Players: " + playersTxtBuilder.substring(0, playersTxtBuilder.length() - 1));

        if (!StringHelper.isEmpty(gameInfo.getWinner())) {
            UUID uuid = nameToUUID.get(gameInfo.getWinner());
            if (uuid != null) {
                gameJson.addProperty("winner", uuid.toString());
            } else {
                gameJson.addProperty("winner", gameInfo.getWinner());
            }
            gameTxt.add("Winner: " + gameInfo.getWinner());
        }

        if (!StringHelper.isEmpty(gameInfo.getFirstBlood())) {
            UUID uuid = nameToUUID.get(gameInfo.getFirstBlood());
            if (uuid != null) {
                gameJson.addProperty("firstblood", uuid.toString());
            } else {
                gameJson.addProperty("firstblood", gameInfo.getFirstBlood());
            }
            gameTxt.add("First Blood: " + gameInfo.getFirstBlood());
        }

        gameTxt.add(" ");
        gameTxt.add("----Actions----");
        //LOGGER.info("Processing actions for game " + gameInfo.getId());
        JsonArray actionsJson = new JsonArray();
        for (GameAction action : gameInfo.getActions()) {
            JsonObject actionObject = new JsonObject();
            actionObject.addProperty("timestamp", action.getTimestamp());
            String formattedTime = dateFormat.format(action.getTimestamp());
            actionObject.addProperty("type", action.getType());
            if (action.getType().equalsIgnoreCase("chat") || action.getType().equalsIgnoreCase("deadchat")) {
                JsonObject chatActionObject = new JsonObject();
                String[] value = action.getValue().split(":");
                chatActionObject.addProperty("sender", value[0]);
                chatActionObject.addProperty("message", value[1]);
                actionObject.add("value", chatActionObject);
            } else if (action.getType().equalsIgnoreCase("death")) {
                JsonObject deathObject = new JsonObject();
                deathObject.addProperty("deathmessage", action.getValue()); //Going to need to change this maybe
                actionObject.add("value", deathObject);
            }  else if (action.getType().equalsIgnoreCase("mutation")) {
                JsonObject mutationObject = new JsonObject();
                String[] rawValue = action.getValue().split(" ");
                mutationObject.addProperty("mutator", rawValue[0]);
                mutationObject.addProperty("target", rawValue[3]);
                actionObject.add("value", mutationObject);
            } else if (action.getType().equalsIgnoreCase("assist")) {
                JsonObject assistObject = new JsonObject();
                String[] rawValue = action.getValue().split(" ");
                assistObject.addProperty("assistor", rawValue[0]);
                assistObject.addProperty("deadplayer", rawValue[5]);
                actionObject.add("value", assistObject);
            } else {
                actionObject.addProperty("value", action.getValue());
            }
            gameTxt.add(formattedTime + " " + action.getType() + " " + action.getValue());
            actionsJson.add(actionObject);
        }
        gameJson.add("actions", actionsJson);

        //LOGGER.info("Saving " + gameInfo.getId() + " to the files.");

        File jsonFile = new File(gameDir, gameInfo.getId() + ".json");
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(GSON.toJson(gameJson));
        }

        File txtFile = new File(gameDir, gameInfo.getId() + ".txt");
        if (!txtFile.exists()) {
            txtFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(txtFile)) {
            for (String gameLine : gameTxt) {
                writer.write(gameLine + "\n");
            }
        }
    }
    
    public UUID getUUID(String name) {
        UUID uuid = null;
        
        if (nameToUUID.containsKey(name)) {
            uuid = nameToUUID.get(name);
        }
        
        if (uuid != null) {
            return uuid;
        }
        
        uuid = NexusAPI.getApi().getPlayerManager().getUUIDFromName(name);
        
        if (uuid != null) {
            nameToUUID.put(name, uuid);
            return uuid;
        }

        try {
            URL url = new URL(UUID_URL.replace("{name}", name));
            URLConnection request = url.openConnection();
            request.connect();

            JsonObject jsonResponse = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
            String rawId = jsonResponse.get("id").getAsString();
            uuid = StringHelper.toUUID(rawId);
            nameToUUID.put(name, uuid);
        } catch (Exception e) {
            nameToUUID.put(name, null);
        }
        return uuid;
    }
}
