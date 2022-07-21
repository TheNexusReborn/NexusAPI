package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.data.annotations.TableInfo;
import com.thenexusreborn.api.data.handler.StatObjectHandler;
import com.thenexusreborn.api.stats.Stat.Info;

import java.util.*;

@TableInfo(value = "statchanges", handler = StatObjectHandler.class)
public class StatChange implements Comparable<StatChange> {
    private Info info;
    
    private int id;
    private final UUID uuid;
    private final Object value;
    private final StatOperator operator;
    private final long timestamp;
    
    public StatChange(Info info, UUID uuid, Object value, StatOperator operator, long timestamp) {
        this.info = info;
        this.uuid = uuid;
        this.value = value;
        this.operator = operator;
        this.timestamp = timestamp;
    }
    
    public StatChange(Info info, int id, UUID uuid, Object value, StatOperator operator, long timestamp) {
        this.info = info;
        this.id = id;
        this.uuid = uuid;
        this.value = value;
        this.operator = operator;
        this.timestamp = timestamp;
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
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public StatType getType() {
        return info.getType();
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
}
