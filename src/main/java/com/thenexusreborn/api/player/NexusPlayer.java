package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.levels.LevelManager;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.util.Operator;

import java.util.*;

//TODO Playtime will be stat based (This will allow messages every 10 minutes as people keep suggesting that
public abstract class NexusPlayer extends CachedPlayer {
    //TODO Move FirstJoined, LastLogin and LastLogout to the stats system
    protected long firstJoined, lastLogin, lastLogout;
    protected Tag tag;
    
    protected Map<String, Stat<Number>> stats = new HashMap<>();
    protected Set<StatChange<Number>> statChanges = new HashSet<>();
    
    protected NexusScoreboard scoreboard;
    
    private UUID lastMessage;
    protected boolean prealpha, alpha, beta;
    
    public NexusPlayer(CachedPlayer cachedPlayer) {
        super(cachedPlayer);
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

//        if (statName.equalsIgnoreCase("xp")) {
//            long playtimeMinutes = (this.playTime / 20) / 60;
//            long playtimeIntervals = playtimeMinutes / 10;
//
//            value = Operator.ADD.calculate(value, getPlayTimeXp());
//        }

        return value.doubleValue();
    }
//    
//    public double getPlayTimeXp() {
//        long playtimeMinutes = (this.playTime / 20) / 60;
//        long playtimeIntervals = playtimeMinutes / 10;
//    
//        return playtimeIntervals * (2 * getRank().getMultiplier());
//    }
    
    public int getLevel() {
//        double xp = getStatValue("xp");
        double xp = 0;
        int playerLevel = 0;
        for (int i = 1; i < LevelManager.levels.size(); i++) {
            if (i == 1) {
                if (xp > LevelManager.levels.get(i)) {
                    playerLevel = i;
                    continue;
                } else {
                    break;
                }
            }
    
            if (xp >= LevelManager.levels.get(i - 1) && xp < LevelManager.levels.get(i)) {
                playerLevel = i - 1;
            }
        }
        
        return playerLevel;
    }
    
    public NexusPlayer getLastMessage() {
        return NexusAPI.getApi().getPlayerManager().getNexusPlayer(this.lastMessage);
    }
    
    public void setLastMessage(NexusPlayer nexusPlayer) {
        this.lastMessage = nexusPlayer.getUniqueId();
    }
    
    public boolean isPrealpha() {
        return prealpha;
    }
    
    public void setPrealpha(boolean prealpha) {
        this.prealpha = prealpha;
    }
    
    public boolean isAlpha() {
        return alpha;
    }
    
    public void setAlpha(boolean alpha) {
        this.alpha = alpha;
    }
    
    public boolean isBeta() {
        return beta;
    }
    
    public void setBeta(boolean beta) {
        this.beta = beta;
    }
    
    //TODO When implementing this method, it should take into account the current server
    public abstract boolean isOnline();
}
