package com.thenexusreborn.api.player;

import me.firestar311.starsql.api.annotations.column.PrimaryKey;
import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.UUID;

@TableName("playertimes")
public class PlayerTime {
    @PrimaryKey private UUID uniqueId;
    private long firstJoined;
    private long lastLogin, lastLogout;
    private long playtime;
    
    private PlayerTime() {}

    public PlayerTime(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public long getFirstJoined() {
        return firstJoined;
    }

    public void setFirstJoined(long firstJoined) {
        this.firstJoined = firstJoined;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }

    public long getPlaytime() {
        return playtime;
    }
    
    public long addPlaytime(long playtime) {
        this.playtime += playtime;
        return this.playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }
}
