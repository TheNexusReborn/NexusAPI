package com.thenexusreborn.api.player;

import com.starmediadev.starsql.annotations.table.TableName;

import java.util.UUID;

@TableName("privatealphausers")
public class PrivateAlphaUser {
    private long id;
    private UUID uuid;
    private String name;
    private long timestamp;
    
    private PrivateAlphaUser() {}
    
    public PrivateAlphaUser(long id, UUID uuid, String name, long timestamp) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.timestamp = timestamp;
    }
    
    @Deprecated
    public PrivateAlphaUser(UUID uuid, long timestamp) {
        this.uuid = uuid;
        this.timestamp = timestamp;
    }
    
    public PrivateAlphaUser(UUID uuid, String name, long timestamp) {
        this(0, uuid, name, timestamp);
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
    
    public String getName() {
        return name;
    }
}