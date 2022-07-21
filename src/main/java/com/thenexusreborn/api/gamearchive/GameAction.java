package com.thenexusreborn.api.gamearchive;

import com.thenexusreborn.api.data.annotations.*;

@TableInfo("gameactions")
public class GameAction {
    @Primary
    private long id;
    private long gameId;
    private long timestamp;
    private String type, value;
    
    public GameAction(long gameId, long timestamp, String type, String value) {
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
    
    public long getGameId() {
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
