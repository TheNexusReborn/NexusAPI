package com.thenexusreborn.api.stats;

import java.util.UUID;

public class Stat {
    private Info info;
    
    private int id;
    private final UUID uuid;
    private Object value;
    private final long created;
    private long modified;
    
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
        this.value = value;
        this.created = created;
        this.modified = modified;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return info.getName();
    }
    
    public Object getValue() {
        return value;
    }
    
    public StatType getType() {
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
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Object getDefaultValue() {
        return info.getDefaultValue();
    }
    
    public static class Info {
        private String name;
        private StatType type;
        private Object defaultValue;
    
        public Info(String name, StatType type, Object defaultValue) {
            this.name = StatHelper.formatStatName(name);
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
    }
}
