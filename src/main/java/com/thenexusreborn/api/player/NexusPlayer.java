package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.levels.LevelManager;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;

import java.util.*;

//TODO Playtime will be stat based (This will allow messages every 10 minutes as people keep suggesting that
public abstract class NexusPlayer extends CachedPlayer {
    //TODO Move FirstJoined, LastLogin and LastLogout to the stats system
    protected long firstJoined, lastLogin, lastLogout;
    protected Tag tag;
    
    protected Map<String, Stat> stats = new HashMap<>();
    protected Set<StatChange> statChanges = new TreeSet<>();
    
    protected NexusScoreboard scoreboard;
    
    private UUID lastMessage;
    protected boolean prealpha, alpha, beta;
    
    public NexusPlayer(CachedPlayer cachedPlayer) {
        super(cachedPlayer);
    }
    
    public NexusPlayer(UUID uniqueId) {
        super(uniqueId);
    }
    
    public NexusPlayer(UUID uniqueId, String name) {
        super(uniqueId, name);
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
    
    public void changeStat(String statName, Object statValue, StatOperator operator) {
        //TODO
    }
    
    public boolean hasStat(String statName) {
        return this.stats.containsKey(statName);
    }
    
    public void addStat(Stat stat) {
        this.stats.put(stat.getName(), stat);
    }
    
    public void addStatChange(StatChange statChange) {
        this.statChanges.add(statChange);
    }
    
    public Set<StatChange> getStatChanges() {
        return statChanges;
    }
    
    public Map<String, Stat> getStats() {
        return stats;
    }
    
    public double getStatValue(String statName) {
        //TODO
        return 0;
    }
    
    public int getLevel() {
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
    
    public Stat getStat(String name) {
        return this.stats.get(StatHelper.formatStatName(name));
    }
}
