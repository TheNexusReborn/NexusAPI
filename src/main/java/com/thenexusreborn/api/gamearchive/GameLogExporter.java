package com.thenexusreborn.api.gamearchive;

import com.google.gson.*;
import com.stardevllc.starlib.helper.StringHelper;
import com.thenexusreborn.api.NexusAPI;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class GameLogExporter {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/{name}";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = NexusAPI.getApi().getLogger();
    private static final Map<String, UUID> nameToUUID = new HashMap<>();

    private File baseDir;

    public GameLogExporter(File baseDir) {
        this.baseDir = baseDir;
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    public void exportGames() throws SQLException, IOException {
        List<GameInfo> gameInfos = NexusAPI.getApi().getPrimaryDatabase().get(GameInfo.class);

        for (GameInfo gameInfo : gameInfos) {
            exportGameInfo(gameInfo);
        }
    }

    public JsonObject getGameJson(int gameId) throws FileNotFoundException {
        File jsonFile = new File(baseDir, gameId + ".json");
        if (!jsonFile.exists()) {
            return null;
        }

        return new JsonParser().parse(new FileReader(jsonFile)).getAsJsonObject();
    }

    public void exportGameInfo(GameInfo gameInfo) throws IOException {
        File gameDir = new File(baseDir, gameInfo.getId() + "");
        if (!gameDir.exists()) {
            gameDir.mkdirs();
        } else {
            return;
        }

        JsonObject gameJson = new JsonObject();

        gameJson.addProperty("id", gameInfo.getId());
        gameJson.addProperty("start", gameInfo.getGameStart());
        gameJson.addProperty("end", gameInfo.getGameEnd());
        gameJson.addProperty("server", gameInfo.getServerName());
        gameJson.addProperty("map", gameInfo.getMapName());
        gameJson.addProperty("playercount", gameInfo.getPlayerCount());
        gameJson.addProperty("length", gameInfo.getLength());

        JsonObject playersObject = new JsonObject();

        for (String playerName : gameInfo.getPlayers()) {
            UUID uuid = getUUID(playerName);

            if (uuid == null) {
                playersObject.addProperty(playerName, "Could not get UUID.");
            } else {
                playersObject.addProperty(playerName, uuid.toString());
            }
        }

        gameJson.add("players", playersObject);

        if (!StringHelper.isEmpty(gameInfo.getWinner())) {
            UUID uuid = nameToUUID.get(gameInfo.getWinner());
            if (uuid != null) {
                gameJson.addProperty("winner", uuid.toString());
            } else {
                gameJson.addProperty("winner", gameInfo.getWinner());
            }
        }

        if (!StringHelper.isEmpty(gameInfo.getFirstBlood())) {
            UUID uuid = nameToUUID.get(gameInfo.getFirstBlood());
            if (uuid != null) {
                gameJson.addProperty("firstblood", uuid.toString());
            } else {
                gameJson.addProperty("firstblood", gameInfo.getFirstBlood());
            }
        }

        JsonArray actionsJson = new JsonArray();
        for (GameAction action : gameInfo.getActions()) {
            JsonObject actionObject = new JsonObject();
            actionObject.addProperty("timestamp", action.getTimestamp());
            actionObject.addProperty("type", action.getType());
            if (action.getVersion() == 1) {
                action.convertFromV1toV2();
                try {
                    NexusAPI.getApi().getPrimaryDatabase().save(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            actionObject.addProperty("version", action.getVersion());
            JsonObject dataObject = new JsonObject();
            action.getValueData().forEach(dataObject::addProperty);
            actionObject.add("data", dataObject);
            
            actionsJson.add(actionObject);
        }
        gameJson.add("actions", actionsJson);

        File jsonFile = new File(gameDir, gameInfo.getId() + ".json");
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(GSON.toJson(gameJson));
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
