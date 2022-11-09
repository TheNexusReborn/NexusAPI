package com.thenexusreborn.api.player;

import com.thenexusreborn.api.storage.annotations.TableInfo;

import java.util.UUID;

@TableInfo("privatealphausers")
public class PrivateAlphaUser {
    private long id;
    private UUID uuid;
    private long timestamp;
    
    private PrivateAlphaUser() {}
    
    public PrivateAlphaUser(long id, UUID uuid, long timestamp) {
        this.id = id;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }
    
    public PrivateAlphaUser(UUID uuid, long timestamp) {
        this.uuid = uuid;
        this.timestamp = timestamp;
    }
    
    public long getId() {
        return id;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}