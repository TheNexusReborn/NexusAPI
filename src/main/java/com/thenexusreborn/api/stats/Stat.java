package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.StatInfoCodec;
import com.thenexusreborn.api.data.handler.StatObjectHandler;

import java.util.*;

@TableInfo(value = "stats", handler = StatObjectHandler.class)
public class Stat {
    @ColumnInfo(name = "name", type = "varchar(100)", codec = StatInfoCodec.class) 
    private Info info;
    
    @Primary 
    private long id;
    private UUID uuid;
    @ColumnInfo(type = "varchar(1000)")
    private Object value;
    @ColumnInfo(type = "varchar(1000)")
    private Object fakedValue;
    private long created;
    private long modified;
    
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
        this.created = created;
        this.modified = modified;
    
        if (info.getType() == StatType.DOUBLE || info.getType() == StatType.INTEGER || info.getType() == StatType.LONG) {
            Number number = (Number) value;
            if (info.getType() == StatType.DOUBLE) {
                this.value = number.doubleValue();
            } else if (info.getType() == StatType.INTEGER) {
                this.value = number.intValue();
            } else if (info.getType() == StatType.LONG) {
                this.value = number.longValue();
            }
        }
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        if (info == null) {
            return null;
        }
        return info.getName();
    }
    
    public Object getValue() {
        return value;
    }
    
    public StatType getType() {
        if (info == null) {
            return null;
        }
        return info.getType();
    }
    
    public void setValue(Object value) {
        this.value = value;
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
        if (info == null) {
            return null;
        }
        return info.getDefaultValue();
    }
    
    public String getDisplayName() {
        return info.getDisplayName();
    }
    
    public Object getFakedValue() {
        return fakedValue;
    }
    
    public void setFakedValue(Object fakedValue) {
        this.fakedValue = fakedValue;
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
        return Objects.equals(info, stat.info) && Objects.equals(uuid, stat.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(info, uuid);
    }
    
    @Override
    public String toString() {
        return "Stat{" +
                "info=" + info +
                ", id=" + id +
                ", uuid=" + uuid +
                ", value=" + value +
                ", fakedValue=" + fakedValue +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
    
    public Info getInfo() {
        return this.info;
    }
    
    @TableInfo(value = "statinfo", handler = StatObjectHandler.class)
    public static class Info {
        @Primary 
        private long id;
        private String name, displayName;
        private StatType type;
        @ColumnInfo(type = "varchar(1000)")
        private Object defaultValue;
        
        private Info() {}
    
        public Info(String name, StatType type, Object defaultValue) {
            this(name, "", type, defaultValue);
        }
    
        public Info(String name, String displayName, StatType type, Object defaultValue) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
            this.defaultValue = defaultValue;
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
    
        public Object getDefaultValue() {
            return defaultValue;
        }
    
        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
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
