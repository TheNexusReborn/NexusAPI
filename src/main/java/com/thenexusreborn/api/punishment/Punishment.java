package com.thenexusreborn.api.punishment;

public class Punishment {
    private int id = -1;
    private final long date, length;
    private final String actor, target, server, reason;
    private final PunishmentType type;
    private final Visibility visibility;
    private PardonInfo pardonInfo;
    private AcknowledgeInfo acknowledgeInfo;
    
    //Cache variables
    private String actorNameCache, targetNameCache;
    
    public Punishment(long date, long length, String actor, String target, String server, String reason, PunishmentType type, Visibility visibility) {
        this.date = date;
        this.length = length;
        this.actor = actor;
        this.target = target;
        this.server = server;
        this.reason = reason;
        this.type = type;
        this.visibility = visibility;
    }
    
    public Punishment(long date, String actor, String target, String server, String reason, PunishmentType type, Visibility visibility) {
        this(date, -1, actor, target, server, reason, type, visibility);
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public long getDate() {
        return date;
    }
    
    public String getTarget() {
        return target;
    }
    
    public long getLength() {
        return length;
    }
    
    public String getActor() {
        return actor;
    }
    
    public String getServer() {
        return server;
    }
    
    public String getReason() {
        return reason;
    }
    
    public PunishmentType getType() {
        return type;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    public PardonInfo getPardonInfo() {
        return pardonInfo;
    }
    
    public void setPardonInfo(PardonInfo pardonInfo) {
        this.pardonInfo = pardonInfo;
    }
    
    public AcknowledgeInfo getAcknowledgeInfo() {
        return acknowledgeInfo;
    }
    
    public void setAcknowledgeInfo(AcknowledgeInfo acknowledgeInfo) {
        this.acknowledgeInfo = acknowledgeInfo;
    }
    
    public boolean isActive() {
        if (this.pardonInfo != null) {
            return false;
        }
        
        if (this.length <= -1) {
            return true;
        }
        
        if (this.type == PunishmentType.WARN) {
            return this.acknowledgeInfo.getTime() > 0;
        }
        
        return System.currentTimeMillis() <= (this.date + this.length);
    }
    
    public String getActorNameCache() {
        return actorNameCache;
    }
    
    public void setActorNameCache(String actorNameCache) {
        this.actorNameCache = actorNameCache;
    }
    
    public String getTargetNameCache() {
        return targetNameCache;
    }
    
    public void setTargetNameCache(String targetNameCache) {
        this.targetNameCache = targetNameCache;
    }
    
    @Override
    public String toString() {
        return "Punishment{" +
                "id=" + id +
                ", date=" + date +
                ", length=" + length +
                ", actor='" + actor + '\'' +
                ", target='" + target + '\'' +
                ", server='" + server + '\'' +
                ", reason='" + reason + '\'' +
                ", type=" + type +
                ", visibility=" + visibility +
                ", pardonInfo=" + pardonInfo +
                ", acknowledgeInfo=" + acknowledgeInfo +
                ", active=" + isActive() +
                '}';
    }
}