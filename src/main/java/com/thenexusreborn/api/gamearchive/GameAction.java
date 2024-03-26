package com.thenexusreborn.api.gamearchive;

import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.Objects;

@SuppressWarnings("ComparatorMethodParameterNotUsed")
@TableName("gameactions")
public class GameAction implements Comparable<GameAction> {
    private long id;
    private long gameId;
    private long timestamp;
    private String type, value;
    
    private GameAction() {}
    
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
    
    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
    
    @Override
    public int compareTo(GameAction o) {
        if (this.timestamp > o.timestamp) {
            return 1;
        }
    
        return -1;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameAction that = (GameAction) o;
        return gameId == that.gameId && timestamp == that.timestamp && Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gameId, timestamp, type, value);
    }
}
