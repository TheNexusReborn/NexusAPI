package com.thenexusreborn.api.gamearchive;

public class GameAction {
    private int gameId;
    private long timestamp;
    private String type, value;
    
    public GameAction(int gameId, long timestamp, String type, String value) {
        this.gameId = gameId;
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
    }
    
    public GameAction(long timestamp, String type, String value) {
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
    }
    
    public int getGameId() {
        return gameId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
}
