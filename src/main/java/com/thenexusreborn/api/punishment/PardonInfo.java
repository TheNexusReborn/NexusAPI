package com.thenexusreborn.api.punishment;

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
        return actorNameCache;
    }
}
