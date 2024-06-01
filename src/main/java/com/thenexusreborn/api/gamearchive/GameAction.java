package com.thenexusreborn.api.gamearchive;

import com.thenexusreborn.api.sql.annotations.column.ColumnCodec;
import com.thenexusreborn.api.sql.annotations.column.ColumnType;
import com.thenexusreborn.api.sql.annotations.table.TableName;
import com.thenexusreborn.api.sql.objects.codecs.ValueDataCodec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ComparatorMethodParameterNotUsed")
@TableName("gameactions")
public class GameAction implements Comparable<GameAction> {
    public static final int CURRENT_VESRION = 2;
    
    private long id;
    private int version = CURRENT_VESRION;
    private long gameId;
    private long timestamp;
    private String type, niceValue;
    @ColumnCodec(ValueDataCodec.class)
    @ColumnType("varchar(10000)")
    private Map<String, String> valueData = new LinkedHashMap<>();
    
    private GameAction() {}
    
    public GameAction(long gameId, long timestamp, String type, String niceValue) {
        this.gameId = gameId;
        this.timestamp = timestamp;
        this.type = type;
        this.niceValue = niceValue;
    }
    
    public GameAction(long timestamp, String type, String niceValue) {
        this.timestamp = timestamp;
        this.type = type;
        this.niceValue = niceValue;
    }
    
    public GameAction addValueData(String key, Object value) {
        valueData.put(key, value.toString());
        return this;
    }

    public Map<String, String> getValueData() {
        return valueData;
    }

    public int getVersion() {
        return version;
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
    
    public String getNiceValue() {
        return niceValue;
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
        return gameId == that.gameId && timestamp == that.timestamp && Objects.equals(type, that.type) && Objects.equals(niceValue, that.niceValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gameId, timestamp, type, niceValue);
    }
}
