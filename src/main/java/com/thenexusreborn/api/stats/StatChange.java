package com.thenexusreborn.api.stats;

import java.util.*;

public class StatChange implements Comparable<StatChange> {
    public static final int version = 1;
    
    private int id;
    private final UUID uuid;
    private final String statName;
    private final StatType type;
    private final Object value;
    private final StatOperator operator;
    private final long timestamp;
    
    public StatChange(UUID uuid, String statName, StatType type, Object value, StatOperator operator, long timestamp) {
        this(-1, uuid, statName, type, value, operator, timestamp);
    }
    
    public StatChange(int id, UUID uuid, String statName, StatType type, Object value, StatOperator operator, long timestamp) {
        this.id = id;
        this.uuid = uuid;
        this.statName = statName;
        this.value = value;
        this.operator = operator;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getStatName() {
        return statName;
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
        return type;
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
        return id == that.id && timestamp == that.timestamp && Objects.equals(uuid, that.uuid) && Objects.equals(statName, that.statName) && Objects.equals(type, that.type) && Objects.equals(value, that.value) && operator == that.operator;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, statName, type, value, operator, timestamp);
    }
}
