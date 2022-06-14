package com.thenexusreborn.api.stats;

import java.util.UUID;

public class Stat {
    private int id;
    private final UUID uuid;
    private final String name;
    private Object value;
    private final long created;
    private long modified;
    
    public Stat(UUID uuid, String name, Object value, long created) {
        this(uuid, name, value, created, created);
    }
    
    public Stat(UUID uuid, String name, Object value, long created, long modified) {
        this(-1, uuid, name, value, created, modified);
    }
    
    public Stat(int id, UUID uuid, String name, Object value, long created, long modified) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.value = value;
        this.created = created;
        this.modified = modified;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public Object getValue() {
        return value;
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
    
    @Override
    public String toString() {
        return "Stat{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
}
