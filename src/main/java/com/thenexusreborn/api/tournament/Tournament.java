package com.thenexusreborn.api.tournament;

import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.StringArrayCodec;

import java.util.*;

@TableInfo("tournaments")
public class Tournament {
    private int id;
    private UUID host;
    private String name;
    private boolean active;
    private int pointsPerKill, pointsPerWin, pointsPerSurvival;
    @ColumnInfo(codec = StringArrayCodec.class)
    private String[] servers;
    
    @ColumnIgnored
    private Map<UUID, ScoreInfo> scoreCache = new HashMap<>();
    
    public Tournament(UUID host, String name) {
        this.host = host;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getHost() {
        return host;
    }
    
    public void setHost(UUID host) {
        this.host = host;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public int getPointsPerKill() {
        return pointsPerKill;
    }
    
    public void setPointsPerKill(int pointsPerKill) {
        this.pointsPerKill = pointsPerKill;
    }
    
    public int getPointsPerWin() {
        return pointsPerWin;
    }
    
    public void setPointsPerWin(int pointsPerWin) {
        this.pointsPerWin = pointsPerWin;
    }
    
    public int getPointsPerSurvival() {
        return pointsPerSurvival;
    }
    
    public void setPointsPerSurvival(int pointsPerSurvival) {
        this.pointsPerSurvival = pointsPerSurvival;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String[] getServers() {
        return servers;
    }
    
    public void setServers(String[] servers) {
        this.servers = servers;
    }
    
    public Map<UUID, ScoreInfo> getScoreCache() {
        return scoreCache;
    }
    
    public static class ScoreInfo implements Comparable<ScoreInfo> {
        private final UUID uuid;
        private final String name;
        private int score;
        private long lastUpdated;
        
        public ScoreInfo(UUID uuid, String name, int score) {
            this.uuid = uuid;
            this.name = name;
            this.score = score;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public int getScore() {
            return score;
        }
        
        public void setScore(int score) {
            this.score = score;
        }
    
        public String getName() {
            return name;
        }
    
        public long getLastUpdated() {
            return lastUpdated;
        }
    
        public void setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ScoreInfo scoreInfo = (ScoreInfo) o;
            return Objects.equals(uuid, scoreInfo.uuid);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }
        
        @Override
        public int compareTo(ScoreInfo o) {
            if (score <= o.score) {
                return 1;
            }
            return -1;
        }
    }
    
    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", host=" + host +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", pointsPerKill=" + pointsPerKill +
                ", pointsPerWin=" + pointsPerWin +
                ", pointsPerSurvival=" + pointsPerSurvival +
                ", servers=" + Arrays.toString(servers) +
                '}';
    }
}
