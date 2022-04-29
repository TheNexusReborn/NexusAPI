package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.MojangHelper;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.util.Operator;

import java.util.*;
import java.util.Map.Entry;

public abstract class NexusPlayer {
    public static final int version = 4;
    
    private static final Map<Integer, Integer> levels = new HashMap<>();
    
    static {
        int xp = 5000;
        for (int i = 1; i <= 100; i++) {
            if (i <= 5) {
                int xpToLevel = i * 1000;
                int totalXp;
                if (i > 1) {
                    totalXp = levels.get(i - 1) + xpToLevel;
                } else {
                    totalXp = xpToLevel;
                }
                levels.put(i, totalXp);
            } else {
                int totalXp = levels.get(i - 1) + 5000;
                levels.put(i, totalXp);
            }
        }
    }
    
    protected final UUID uniqueId;
    protected long firstJoined, lastLogin, lastLogout, playTime;
    protected String lastKnownName;
    protected Tag tag;
    
    protected Map<Rank, Long> ranks = new TreeMap<>();
    
    protected Map<String, Stat<Number>> stats = new HashMap<>();
    protected Set<StatChange<Number>> statChanges = new HashSet<>();
    
    protected Set<Tag> unlockedTags = new HashSet<>();
    
    protected NexusScoreboard scoreboard;
    
    public NexusPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.lastKnownName = name;
        this.ranks.put(Rank.MEMBER, -1L);
        this.firstJoined = System.currentTimeMillis();
    }
    
    public NexusPlayer(UUID uniqueId, Map<Rank, Long> ranks, long firstJoined, long lastLogin, long lastLogout, long playTime, String lastKnownName, Tag tag, Set<Tag> unlockedTags) {
        this.uniqueId = uniqueId;
        this.ranks.putAll(ranks);
        this.firstJoined = firstJoined;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.playTime = playTime;
        this.lastKnownName = lastKnownName;
        this.tag = tag;
        this.unlockedTags = unlockedTags;
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
        //System.out.println("Getting name for player " + this.uniqueId);
        //System.out.println("Existing name " + this.lastKnownName);
        String nameFromServer = getNameFromServer();
        //System.out.println("Name from server is " + nameFromServer);
        if (nameFromServer != null && !nameFromServer.equals("")) {
            //System.out.println("Checking for name updates");
            if (this.lastKnownName == null || !this.lastKnownName.equalsIgnoreCase(nameFromServer)) {
                //System.out.println("Stored name is not equal to the name form the server, updating");
                this.lastKnownName = nameFromServer;
            }
        } else if (this.lastKnownName == null || this.lastKnownName.equals("") || this.lastKnownName.equalsIgnoreCase("null")) {
            //System.out.println("Stored name is invalid, updating from Mojang API");
            String nameFromApi = MojangHelper.getNameFromUUID(this.uniqueId);
            //System.out.println("Name from Mojang is " + nameFromApi);
            if (nameFromApi != null && !nameFromApi.equals("")) {
                this.lastKnownName = nameFromApi;
                //System.out.println("Updated stored name.");
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
    
    public <T extends Number> void changeStat(String statName, T statValue, Operator operator) {
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
    
    public double getStatValue(String statName) {
        Number value = 0;
        Stat<Number> stat = this.stats.get(statName);
        if (stat != null) {
            value = stat.getValue();
        }
        
        for (StatChange<Number> statChange : this.statChanges) {
            if (statChange.getStatName().equalsIgnoreCase(statName)) {
                value = statChange.getOperator().calculate(value, statChange.getValue());
            }
        }
        return value.doubleValue();
    }
    
    public int getLevel() {
        double xp = getStatValue("xp");
    
        long playtimeMinutes = (this.playTime / 20) / 60;
        long playtimeIntervals = playtimeMinutes / 10;
        
        xp += playtimeIntervals * (2 * getRank().getMultiplier());
    
        int playerLevel = 0;
        for (int i = 1; i < levels.size(); i++) {
            if (i == 1) {
                if (xp > levels.get(i)) {
                    playerLevel = i;
                    continue;
                } else {
                    break;
                }
            }
    
            if (xp >= levels.get(i - 1) && xp < levels.get(i)) {
                playerLevel = i;
            }
        }
        
        return playerLevel;
    }
    
    public void setRanks(Map<Rank, Long> ranks) {
        this.ranks = ranks;
    }
    
    public Set<Tag> getUnlockedTags() {
        return unlockedTags;
    }
    
    public void unlockTag(Tag tag) {
        this.unlockedTags.add(tag);
    }
    
    public boolean isTagUnlocked(Tag tag) {
        return this.unlockedTags.contains(tag);
    }
    
    public void setUnlockedTags(Set<Tag> tags) {
        this.unlockedTags = tags;
    }
}
