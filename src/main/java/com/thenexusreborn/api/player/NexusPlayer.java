package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.ColumnIgnored;
import com.thenexusreborn.api.levels.LevelManager;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.StatOperator;

import java.util.*;

public abstract class NexusPlayer extends CachedPlayer {
    protected NexusScoreboard scoreboard;
    protected UUID lastMessage;
    
    public NexusPlayer(NexusPlayer cachedPlayer) {
        super(cachedPlayer);
        this.scoreboard = cachedPlayer.scoreboard;
        this.lastMessage = cachedPlayer.lastMessage;
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
    
    public void setScoreboard(NexusScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
    
    @Deprecated
    public long getFirstJoined() {
        return (long) getStatValue("firstjoined");
    }
    
    @Deprecated
    public void setFirstJoined(long firstJoined) {
        changeStat("firstjoined", firstJoined, StatOperator.SET);
    }
    
    @Deprecated
    public long getLastLogin() {
        return (long) getStatValue("lastlogin");
    }
    
    @Deprecated
    public void setLastLogin(long lastLogin) {
        changeStat("lastlogin", lastLogin, StatOperator.SET);
    }
    
    @Deprecated
    public void setUnlockedTags(Set<String> unlockedTags) {
        changeStat("unlockedtags", unlockedTags, StatOperator.SET);
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
    
    @Deprecated
    public long getLastLogout() {
        return (long) getStatValue("lastlogout");
    }
    
    @Deprecated
    public void setLastLogout(long lastLogout) {
        changeStat("lastlogout", lastLogout, StatOperator.SET);
    }
    
    @Deprecated
    public int getLevel() {
        double xp = (double) getStatValue("xp");
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
    
    @Deprecated
    public boolean isPrealpha() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setPrealpha(boolean prealpha) {
        changeStat("prealpha", prealpha, StatOperator.SET);
    }
    
    @Deprecated
    public boolean isAlpha() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setAlpha(boolean alpha) {
        changeStat("alpha", alpha, StatOperator.SET);
    }
    
    @Deprecated
    public boolean isBeta() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setBeta(boolean beta) {
        changeStat("beta", beta, StatOperator.SET);
    }
    
    public abstract boolean isOnline();
    
}
