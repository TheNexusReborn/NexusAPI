package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.storage.annotations.*;
import com.thenexusreborn.api.storage.codec.StatInfoCodec;
import com.thenexusreborn.api.storage.handler.StatObjectHandler;
import com.thenexusreborn.api.stats.Stat.Info;

import java.util.*;

@TableInfo(value = "statchanges", handler = StatObjectHandler.class)
public class StatChange implements Comparable<StatChange> {
    @ColumnInfo(type = "varchar(100)", codec = StatInfoCodec.class)
    private Info info;
    
    @Primary
    private long id;
    private UUID uuid;
    @ColumnInfo(type = "varchar(1000)")
    private Object value;
    private boolean fake;
    private StatOperator operator;
    private long timestamp;
    
    private StatChange() {}
    
    public StatChange(Info info, UUID uuid, Object value, StatOperator operator, long timestamp) {
        this(info, 0, uuid, value, operator, false, timestamp);
    }
    
    public StatChange(Info info, long id, UUID uuid, Object value, StatOperator operator, long timestamp) {
        this(info, uuid, value, operator, false, timestamp);
    }
    
    public StatChange(Info info, UUID uuid, Object value, StatOperator operator, boolean fake, long timestamp) {
        this(info, 0, uuid, value, operator, fake, timestamp);
    }
    
    public StatChange(Info info, long id, UUID uuid, Object value, StatOperator operator, boolean fake, long timestamp) {
        this.info = info;
        this.id = id;
        this.uuid = uuid;
        this.value = value;
        this.operator = operator;
        this.timestamp = timestamp;
        this.fake = fake;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getStatName() {
        return info.getName();
    }
    
    public Object getValue() {
        return value;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public StatOperator getOperator() {
        return operator;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public StatType getType() {
        return info.getType();
    }
    
    public boolean isFake() {
        return fake;
    }
    
    @Override
    public int compareTo(StatChange o) {
        if (timestamp <= o.timestamp) {
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
        StatChange that = (StatChange) o;
        return id == that.id && timestamp == that.timestamp && Objects.equals(uuid, that.uuid) && Objects.equals(info.getName(), that.info.getName()) && Objects.equals(info.getType(), that.info.getType()) && Objects.equals(value, that.value) && operator == that.operator;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, info.getName(), info.getType(), value, operator, timestamp);
    }
    
    @Override
    public String toString() {
        return "StatChange{" +
                "info=" + info +
                ", id=" + id +
                ", uuid=" + uuid +
                ", value=" + value +
                ", fake=" + fake +
                ", operator=" + operator +
                ", timestamp=" + timestamp +
                '}';
    }
}
