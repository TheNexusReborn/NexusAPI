package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.MojangHelper;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.util.Operator;

import java.util.*;
import java.util.Map.Entry;

public abstract class NexusPlayer {
    public static final int version = 3;
    
    protected final UUID uniqueId;
    protected long firstJoined, lastLogin, lastLogout, playTime;
    protected String lastKnownName;
    protected Tag tag;
    
    protected Map<Rank, Long> ranks = new TreeMap<>();
    
    protected Map<String, Stat<Number>> stats = new HashMap<>();
    protected Set<StatChange<Number>> statChanges = new HashSet<>();
    
    protected NexusScoreboard scoreboard;
    
    public NexusPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.lastKnownName = name;
        this.ranks.put(Rank.MEMBER, -1L);
        this.firstJoined = System.currentTimeMillis();
    }
    
    public NexusPlayer(UUID uniqueId, Map<Rank, Long> ranks, long firstJoined, long lastLogin, long lastLogout, long playTime, String lastKnownName, Tag tag) {
        this.uniqueId = uniqueId;
        this.ranks.putAll(ranks);
        this.firstJoined = firstJoined;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.playTime = playTime;
        this.lastKnownName = lastKnownName;
        this.tag = tag;
    }
    
    public NexusScoreboard getScoreboard() {
        return scoreboard;
    }
    
    public Tag getTag() {
        return tag;
    }
    
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    public void setScoreboard(NexusScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public Map<Rank, Long> getRanks() {
        return ranks;
    }
    
    public Rank getRank() {
        if (ranks.size() == 0) {
            return Rank.MEMBER;
        }
    
        Rank highestRank = null;
        Iterator<Entry<Rank, Long>> iterator = ranks.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Rank, Long> entry = iterator.next();
            if (entry.getValue() < System.currentTimeMillis()) {
                if (entry.getValue() != -1) {
                    iterator.remove();
                    continue;
                }
            }
            
            if (highestRank == null) {
                highestRank = entry.getKey();
            } else {
                if (entry.getKey().ordinal() < highestRank.ordinal()) {
                    highestRank = entry.getKey();
                }
            }
        }
    
        return highestRank;
    }
    
    public void setRank(Rank rank, long time) {
        this.ranks.put(rank, time);
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
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public void incrementPlayTime() {
        this.playTime++;
    }
    
    public String getLastKnownName() {
        return lastKnownName;
    }
    
    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }
    
    public String getName() {
        String nameFromServer = getNameFromServer();
        if (nameFromServer != null || !nameFromServer.equals("")) {
            if (this.lastKnownName == null || !this.lastKnownName.equalsIgnoreCase(nameFromServer)) {
                this.lastKnownName = nameFromServer;
            }
        } else if (this.lastKnownName == null || this.lastKnownName.equals("") || this.lastKnownName.equalsIgnoreCase("null")) {
            String nameFromApi = MojangHelper.getNameFromUUID(this.uniqueId);
            if (nameFromApi != null && !nameFromApi.equals("")) {
                this.lastKnownName = nameFromApi;
            }
        }
        
        return lastKnownName;
    }
    
    public abstract String getNameFromServer();
    
//    public Player getPlayer() {
//        return Bukkit.getPlayer(uniqueId);
//    }
    
    public String getDisplayName() {
        if (getRank() != Rank.MEMBER) {
            return getRank().getPrefix() + " &f" + getName();
        } else {
            return getRank().getPrefix() + getName();
        }
    }
    
    public abstract void sendMessage(String message);
    
    public String getTablistName() {
        if (getRank() == Rank.MEMBER) {
            return Rank.MEMBER.getColor() + getName();
        } else {
            return "&f" + getName();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NexusPlayer that = (NexusPlayer) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
    
    public void removeRank(Rank rank) {
        this.ranks.remove(rank);
    }
    
    public long getLastLogout() {
        return lastLogout;
    }
    
    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }
    
    public <T extends Number> void setStat(String statName, T statValue, Operator operator) {
        StatChange<T> statChange = new StatChange<>(this.uniqueId, statName, statValue, operator, System.currentTimeMillis());
        NexusAPI.getApi().getDataManager().pushStatChangeAsync(statChange);
        this.statChanges.add((StatChange<Number>) statChange);
    }
    
    /**
     * This is a temporary method, this will be done either on the Proxy server or on a separate server, during PreAlpha/Alpha, there is only a single server
     */
    public void consolodateStats() {
        Set<StatChange<Number>> statChanges = new TreeSet<>(this.statChanges);
        
        for (StatChange<Number> statChange : statChanges) {
            Stat<Number> stat = this.stats.get(statChange.getStatName());
            if (stat != null) {
                if (statChange.getValue().getClass().equals(stat.getValue().getClass())) {
                    if (statChange.getValue() instanceof Number) {
                        stat.setValue(statChange.getOperator().calculate(stat.getValue(), statChange.getValue()));
                    }
                }
            } else {
                stat = new Stat<>(statChange.getUuid(), statChange.getStatName(), statChange.getValue(), statChange.getTimestamp());
                this.stats.put(stat.getName(), stat);
            }
        }
    
        for (Stat<?> stat : this.stats.values()) {
            NexusAPI.getApi().getDataManager().pushStatAsync(stat);
        }
    
        for (StatChange<?> statChange : statChanges) {
            this.statChanges.remove(statChange);
            NexusAPI.getApi().getDataManager().removeStatChangeAsync(statChange);
        }
    }
    
    public boolean hasStat(String statName) {
        return this.stats.containsKey(statName);
    }
    
    public void addStat(Stat<Number> stat) {
        this.stats.put(stat.getName(), stat);
    }
    
    public void addStatChange(StatChange<Number> statChange) {
        this.statChanges.add(statChange);
    }
    
    public Set<StatChange<Number>> getStatChanges() {
        return statChanges;
    }
    
    public Map<String, Stat<Number>> getStats() {
        return stats;
    }
    
    @Override
    public String toString() {
        return "NexusPlayer{" +
                "uniqueId=" + uniqueId +
                ", lastKnownName='" + lastKnownName + '\'' +
                '}';
    }
}
