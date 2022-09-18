package com.thenexusreborn.api.punishment;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.CachedPlayer;

import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class PardonInfo {
    private final long date;
    private final String actor, reason;
    private String actorNameCache;
    
    public PardonInfo(long date, String actor, String reason) {
        this.date = date;
        this.actor = actor;
        this.reason = reason;
    }
    
    public long getDate() {
        return date;
    }
    
    public String getActor() {
        return actor;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "PardonInfo{" +
                "date=" + date +
                ", actor='" + actor + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
    
    public void setActorNameCache(String actorNameCache) {
        this.actorNameCache = actorNameCache;
    }
    
    public String getActorNameCache() {
        if (actorNameCache == null) {
            try {
                UUID uuid = UUID.fromString(getActor());
                CachedPlayer actorCachePlayer = NexusAPI.getApi().getPlayerManager().getCachedPlayers().get(uuid);
                if (actorCachePlayer == null) {
                    actorNameCache = actor;
                } else {
                    actorNameCache = actorCachePlayer.getName();
                }
            } catch (Exception e) {
                this.actorNameCache = actor;
            }
        }
        
        return actorNameCache;
    }
}
