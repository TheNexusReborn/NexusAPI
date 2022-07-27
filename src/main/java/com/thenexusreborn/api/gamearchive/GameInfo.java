package com.thenexusreborn.api.gamearchive;

import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.StringArrayCodec;
import com.thenexusreborn.api.data.handler.GamesObjectHandler;

import java.util.*;

@TableInfo(value = "games", handler = GamesObjectHandler.class)
public class GameInfo {
    @Primary
    private long id;
    private long gameStart, gameEnd;
    private String serverName;
    @ColumnInfo(type = "varchar(1000)", codec = StringArrayCodec.class) 
    private String[] players;
    private String winner, mapName, settings, firstBlood;
    private int playerCount;
    private long length;
    @ColumnIgnored
    private List<GameAction> actions = new ArrayList<>();
    
    public GameInfo() {
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
    
    public List<GameAction> getActions() {
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
    
    public void setPlayers(String[] players) {
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
}
