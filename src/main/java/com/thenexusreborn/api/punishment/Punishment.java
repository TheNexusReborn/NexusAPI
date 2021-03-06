package com.thenexusreborn.api.punishment;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.*;
import com.thenexusreborn.api.player.NexusPlayer;

import java.sql.*;
import java.util.UUID;

public class Punishment implements Comparable<Punishment> {
    public static final String KICK_FORMAT = "&d&lThe Nexus Reborn &7- {type}\n \n" +
            "&fStaff: &a{actor}\n" +
            "&fReason: &b{reason}\n" +
            "&fExpires: &c{expire}\n" +
            "&fPunishment ID: &e{id}";
    
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
        
        try {
            UUID uuid = UUID.fromString(getActor());
            NexusPlayer actorCachePlayer = NexusAPI.getApi().getPlayerManager().getNexusPlayer(uuid);
            if (actorCachePlayer == null) {
                try (Connection connection = NexusAPI.getApi().getConnection(); Statement s = connection.createStatement()) {
                    ResultSet rs = s.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
                    if (rs.next()) {
                        actorNameCache = rs.getString("lastKnownName");
                    }
                }
            } else {
                actorNameCache = actorCachePlayer.getName();
            }
        } catch (Exception e) {
            this.actorNameCache = actor;
        }
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
    
        if (this.type == PunishmentType.WARN) {
            return !(this.acknowledgeInfo.getTime() > 0);
        }
        
        if (getTimeRemaining() > 0) {
            return true;
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
    
    public long getTimeRemaining() {
        if (getLength() == -1) {
            return -1;
        }
        
        long timeRemaining = (this.date + this.length) - System.currentTimeMillis();
        return timeRemaining > 0 ? timeRemaining : -2;
    }
    
    public String formatTimeLeft() {
        String expires = "";
        long timeRemaining = getTimeRemaining();
        if (type == PunishmentType.KICK || type == PunishmentType.WARN) {
            return expires;
        }
        if (timeRemaining == -1) {
            expires = "Permanent";
        } else if (timeRemaining == -2) {
            expires = "Expired";
        } else if (getTimeRemaining() >= 1) {
            expires = TimeHelper.formatTime(timeRemaining);
        }
        return expires;
    }
    
    public String formatKick() {
        String message = Punishment.KICK_FORMAT;
        message = message.replace("{type}", getType().getColor() + StringHelper.capitalizeEveryWord(getType().getVerb()));
        message = message.replace("{actor}", getActorNameCache());
        message = message.replace("{reason}", getReason());
        message = message.replace("{expire}", formatTimeLeft());
        message = message.replace("{id}", getId() + "");
        return message;
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
    
    @Override
    public int compareTo(Punishment o) {
        return Integer.compare(this.id, o.id);
    }
}