package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.frameworks.value.*;
import com.thenexusreborn.api.storage.annotations.*;

import java.util.*;

@TableInfo(value = "stats")
public class Stat implements Cloneable {
    @Primary 
    private long id;
    private String name;
    private UUID uuid;
    @ColumnInfo(type = "varchar(1000)", codec = ValueCodec.class)
    private Value value;
    @ColumnInfo(type = "varchar(1000)", codec = ValueCodec.class)
    private Value fakedValue;
    private long created;
    private long modified;
    
    @ColumnIgnored
    private Info info;
    
    private Stat() {}
    
    public Stat(Info info, int id, UUID uuid, long created, long modified) {
        this(info, id, uuid, info.getDefaultValue(), created, modified);
    }
    
    public Stat(Info info, UUID uuid, long created, long modified) {
        this(info, 0, uuid, created, modified);
    }
    
    public Stat(Info info, UUID uuid, long created) {
        this(info, uuid, created, created);
    }
    
    public Stat(Info info, UUID uuid, Object value, long created) {
        this(info, uuid, value, created, created);
    }
    
    public Stat(Info info, UUID uuid, Object value, long created, long modified) {
        this(info, -1, uuid, value, created, modified);
    }
    
    public Stat(Info info, int id, UUID uuid, Object value, long created, long modified) {
        this.info = info;
        this.id = id;
        this.uuid = uuid;
        this.name = info.getName();
        this.created = created;
        this.modified = modified;
        if (value instanceof Value) {
            this.value = (Value) value;
        } else {
            this.value = new Value(info.getType().getValueType(), value);
        }
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return getInfo().getName();
    }
    
    public Value getValue() {
        if (this.value == null) {
            this.value = new Value(getType().getValueType(), getDefaultValue());
        }
        return value;
    }
    
    public StatType getType() {
        return getInfo().getType();
    }
    
    public void setValue(Object value) {
        if (this.value == null) {
            this.value = new Value(getInfo().getType().getValueType(), value);
        } else {
            this.value.set(value);
        }
        this.modified = System.currentTimeMillis();
    }
    
    public long getCreated() {
        return created;
    }
    
    public long getModified() {
        return modified;
    }
    
    public void setModified(long modified) {
        this.modified = modified;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Object getDefaultValue() {
        return getInfo().getDefaultValue();
    }
    
    public String getDisplayName() {
        return getInfo().getDisplayName();
    }
    
    public Value getFakedValue() {
        return fakedValue;
    }
    
    public void setFakedValue(Object fakedValue) {
        if (this.fakedValue == null) {
            this.fakedValue = new Value(getInfo().getType().getValueType(), fakedValue);
        } else {
            this.fakedValue.set(fakedValue);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Stat stat = (Stat) o;
        return Objects.equals(getInfo(), stat.getInfo()) && Objects.equals(uuid, stat.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getInfo(), uuid);
    }
    
    @Override
    public String toString() {
        return "Stat{" +
                "info=" + getInfo() +
                ", id=" + id +
                ", uuid=" + uuid +
                ", value=" + value +
                ", fakedValue=" + fakedValue +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
    
    public Info getInfo() {
        if (this.info == null) {
            this.info = StatHelper.getInfo(this.name);
        }
        return this.info;
    }
    
    @Override
    public Stat clone() {
        return new Stat(this.getInfo(), 0, this.uuid, this.value.get(), System.currentTimeMillis(), System.currentTimeMillis());
    }
    
    @TableInfo(value = "statinfo")
    public static class Info {
        @Primary 
        private long id;
        private String name, displayName;
        private StatType type;
        @ColumnInfo(type = "varchar(1000)", codec = ValueCodec.class)
        private Value defaultValue;
        
        private Info() {}
    
        public Info(String name, StatType type, Object defaultValue) {
            this(name, "", type, defaultValue);
        }
    
        public Info(String name, String displayName, StatType type, Object defaultValue) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
            this.defaultValue = new Value(type.getValueType(), defaultValue);
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public StatType getType() {
            return type;
        }
    
        public void setType(StatType type) {
            this.type = type;
        }
    
        public Value getDefaultValue() {
            if (this.defaultValue == null) {
                this.defaultValue = new Value(getType().getValueType(), getDefaultValue());
            }
            return defaultValue;
        }
    
        public void setDefaultValue(Object value) {
            if (this.defaultValue == null) {
                this.defaultValue = new Value(getType().getValueType(), value);
            } else {
                this.defaultValue.set(value);
            }
        }
    
        public String getDisplayName() {
            return displayName;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Info info = (Info) o;
            return Objects.equals(name, info.name) && type == info.type;
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    
        @Override
        public String toString() {
            return "Info{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", type=" + type +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }
}
