package com.thenexusreborn.api.gamearchive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thenexusreborn.api.NexusAPI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GameLogExporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    public void exportGameInfo(GameInfo gameInfo) throws IOException {
        JsonObject gameJson = gameInfo.toJson();

        File jsonFile = new File(baseDir, gameInfo.getId() + ".json");
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(GSON.toJson(gameJson));
        }
    }
}
