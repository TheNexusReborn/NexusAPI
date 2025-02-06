package com.thenexusreborn.api.gamearchive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stardevllc.helper.StringHelper;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.PlayerManager;
import com.thenexusreborn.api.sql.annotations.column.ColumnCodec;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.column.ColumnType;
import com.thenexusreborn.api.sql.annotations.table.TableHandler;
import com.thenexusreborn.api.sql.annotations.table.TableName;
import com.thenexusreborn.api.sql.objects.codecs.StringArrayCodec;
import com.thenexusreborn.api.sql.objects.objecthandler.GamesObjectHandler;

import java.util.*;

@TableName("games")
@TableHandler(GamesObjectHandler.class)
public class GameInfo implements Comparable<GameInfo> {
    private long id;
    private long gameStart, gameEnd;
    private String serverName;
    @ColumnType("varchar(1000)")
    @ColumnCodec(StringArrayCodec.class)
    private String[] players;
    private String winner, mapName, settings, firstBlood;
    private int playerCount;
    private long length;
    @ColumnIgnored
    private final Set<GameAction> actions = new TreeSet<>();
    
    public GameInfo() {
    }
    
    public GameInfo(JsonObject json) {
        this.id = json.get("id").getAsLong();
        this.gameStart = json.get("start").getAsLong();
        this.gameEnd = json.get("end").getAsLong();
        this.serverName = json.get("server").getAsString();
        this.mapName = json.get("map").getAsString();
        this.playerCount = json.get("playercount").getAsInt();
        this.length = json.get("length").getAsLong();
        
        JsonObject playersObject = json.getAsJsonObject("players");
        this.players = new String[playerCount];
        int counter = 0;
        for (Map.Entry<String, JsonElement> playerEntry : playersObject.entrySet()) {
            this.players[counter] = playerEntry.getKey();
            counter++;
        }
        
        PlayerManager playerManager = NexusAPI.getApi().getPlayerManager();
        
        UUID winnerUUID = UUID.fromString(json.get("winner").getAsString());
        this.winner = playerManager.getNameFromUUID(winnerUUID);
        
        UUID firstBloodUUID = UUID.fromString(json.get("firstblood").getAsString());
        this.firstBlood = playerManager.getNameFromUUID(firstBloodUUID);
        
        JsonObject actionsObject = json.getAsJsonObject("actions");
        for (Map.Entry<String, JsonElement> actionEntry : actionsObject.entrySet()) {
            GameAction gameAction = new GameAction(actionEntry.getValue().getAsJsonObject());
            this.actions.add(gameAction);
        }
    }
    
    public JsonObject toJson() {
        JsonObject gameJson = new JsonObject();

        gameJson.addProperty("id", getId());
        gameJson.addProperty("start", getGameStart());
        gameJson.addProperty("end", getGameEnd());
        gameJson.addProperty("server", getServerName());
        gameJson.addProperty("map", getMapName());
        gameJson.addProperty("playercount", getPlayerCount());
        gameJson.addProperty("length", getLength());

        JsonObject playersObject = new JsonObject();

        PlayerManager playerManager = NexusAPI.getApi().getPlayerManager();

        for (String playerName : getPlayers()) {
            UUID uuid = playerManager.getUUIDFromName(playerName);

            if (uuid == null) {
                playersObject.addProperty(playerName, "Could not get UUID.");
            } else {
                playersObject.addProperty(playerName, uuid.toString());
            }
        }

        gameJson.add("players", playersObject);

        if (!StringHelper.isEmpty(getWinner())) {
            UUID uuid = playerManager.getUUIDFromName(getWinner());
            if (uuid != null) {
                gameJson.addProperty("winner", uuid.toString());
            } else {
                gameJson.addProperty("winner", getWinner());
            }
        }

        if (!StringHelper.isEmpty(getFirstBlood())) {
            UUID uuid = playerManager.getUUIDFromName(getFirstBlood());
            if (uuid != null) {
                gameJson.addProperty("firstblood", uuid.toString());
            } else {
                gameJson.addProperty("firstblood", getFirstBlood());
            }
        }

        JsonObject actionsJson = new JsonObject();
        for (GameAction action : getActions()) {
            actionsJson.add(String.valueOf(action.getTimestamp()), action.toJson());
        }
        gameJson.add("actions", actionsJson);
        return gameJson;
    }
    
    public GameInfo(long id, long gameStart, long gameEnd, String serverName, String[] players, String winner, String mapName, String settings, String firstBlood, int playerCount, long length) {
        this.id = id;
        this.gameStart = gameStart;
        this.gameEnd = gameEnd;
        this.serverName = serverName;
        this.players = players;
        this.winner = winner;
        this.mapName = mapName;
        this.settings = settings;
        this.firstBlood = firstBlood;
        this.playerCount = playerCount;
        this.length = length;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public void setWinner(String winner) {
        this.winner = winner;
    }
    
    public long getId() {
        return id;
    }
    
    public long getGameStart() {
        return gameStart;
    }
    
    public long getGameEnd() {
        return gameEnd;
    }
    
    public String[] getPlayers() {
        return players;
    }
    
    public String getMapName() {
        return mapName;
    }
    
    public String getSettings() {
        return settings;
    }
    
    public String getFirstBlood() {
        return firstBlood;
    }
    
    public int getPlayerCount() {
        return playerCount;
    }
    
    public long getLength() {
        return length;
    }
    
    public Set<GameAction> getActions() {
        return actions;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setGameStart(long gameStart) {
        this.gameStart = gameStart;
    }
    
    public void setGameEnd(long gameEnd) {
        this.gameEnd = gameEnd;
    }
    
    public void setPlayers(String... players) {
        this.players = players;
    }
    
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    
    public void setSettings(String settings) {
        this.settings = settings;
    }
    
    public void setFirstBlood(String firstBlood) {
        this.firstBlood = firstBlood;
    }
    
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
    
    public void setLength(long length) {
        this.length = length;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameInfo gameInfo = (GameInfo) o;
        return gameStart == gameInfo.gameStart && gameEnd == gameInfo.gameEnd && playerCount == gameInfo.playerCount && length == gameInfo.length && Objects.equals(serverName, gameInfo.serverName) && Arrays.equals(players, gameInfo.players) && Objects.equals(winner, gameInfo.winner) && Objects.equals(mapName, gameInfo.mapName) && Objects.equals(settings, gameInfo.settings) && Objects.equals(firstBlood, gameInfo.firstBlood);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(gameStart, gameEnd, serverName, winner, mapName, settings, firstBlood, playerCount, length);
        result = 31 * result + Arrays.hashCode(players);
        return result;
    }
    
    @Override
    public int compareTo(GameInfo o) {
        return Long.compare(this.id, o.id);
    }
}
