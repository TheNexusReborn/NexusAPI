package com.thenexusreborn.api.stats;

import com.starmediadev.starlib.util.Value;
import com.starmediadev.starsql.annotations.column.*;
import com.starmediadev.starsql.annotations.table.TableName;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.stats.Stat.Info;

import java.util.*;

@TableName(value = "statchanges")
public class StatChange implements Comparable<StatChange> {
    
    private long id;
    private UUID uuid;
    private String name;
    @ColumnType("varchar(1000)")
    private Value value;
    private boolean fake;
    private StatOperator operator;
    private long timestamp;
    
    @ColumnIgnored 
    private Info info;
    
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
        this.name = info.getName();
        this.value = new Value(info.getType().getValueType(), value);
        this.operator = operator;
        this.timestamp = timestamp;
        this.fake = fake;
    }
    
    public Info getInfo() {
        if (this.info == null) {
            this.info = StatHelper.getInfo(this.name);
        } 
        return this.info;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getStatName() {
        return getInfo().getName();
    }
    
    public Value getValue() {
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
        return getInfo().getType();
    }
    
    public boolean isFake() {
        return fake;
    }
    
    public StatChange push() {
        NexusAPI.getApi().getScheduler().runTaskAsynchronously(() -> NexusAPI.getApi().getPrimaryDatabase().saveSilent(this));
        return this;
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
        return id == that.id && timestamp == that.timestamp && Objects.equals(uuid, that.uuid) && Objects.equals(info.getName(), that.info.getName()) && info.getType() == that.info.getType() && Objects.equals(value, that.value) && operator == that.operator;
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
