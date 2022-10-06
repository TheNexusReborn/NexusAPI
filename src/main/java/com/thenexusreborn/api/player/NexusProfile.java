package com.thenexusreborn.api.player;

import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.RanksCodec;
import com.thenexusreborn.api.stats.StatOperator;

import java.util.*;

public abstract class NexusProfile {
    
    @Primary
    protected long id;
    protected UUID uniqueId;
    protected String name;
    @ColumnIgnored
    protected Set<IPEntry> ipHistory = new HashSet<>();
    @ColumnInfo(type = "varchar(1000)", codec = RanksCodec.class)
    protected PlayerRanks playerRanks;
    @ColumnIgnored
    protected PlayerStats playerStats;
    @ColumnIgnored
    protected PlayerToggles playerToggles;
    
    protected NexusProfile() {
    }
    
    public NexusProfile(UUID uniqueId) {
        this.uniqueId = uniqueId;
        playerStats = new PlayerStats(uniqueId);
        this.playerToggles = new PlayerToggles();
        this.playerStats = new PlayerStats(uniqueId);
    }
    
    public long getFirstJoined() {
        return (long) getStats().getValue("firstjoined");
    }
    
    public void setFirstJoined(long firstJoined) {
        getStats().change("firstjoined", firstJoined, StatOperator.SET);
    }
    
    public long getLastLogin() {
        return (long) getStats().getValue("lastlogin");
    }
    
    public void setLastLogin(long lastLogin) {
        getStats().change("lastlogin", lastLogin, StatOperator.SET);
    }
    
    public void setUnlockedTags(Set<String> unlockedTags) {
        getStats().change("unlockedtags", unlockedTags, StatOperator.SET);
    }
    
    public String getDisplayName() {
        if (getRanks().get() != Rank.MEMBER) {
            return getRanks().get().getPrefix() + " &f" + getName();
        } else {
            return getRanks().get().getPrefix() + getName();
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
        NexusProfile that = (NexusProfile) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
    
    public long getLastLogout() {
        return (long) getStats().getValue("lastlogout");
    }
    
    public void setLastLogout(long lastLogout) {
        getStats().change("lastlogout", lastLogout, StatOperator.SET);
    }
    
    public boolean isPrealpha() {
        return (boolean) getStats().getValue("prealpha");
    }
    
    public void setPrealpha(boolean prealpha) {
        getStats().change("prealpha", prealpha, StatOperator.SET);
    }
    
    public boolean isAlpha() {
        return (boolean) getStats().getValue("prealpha");
    }
    
    public void setAlpha(boolean alpha) {
        getStats().change("alpha", alpha, StatOperator.SET);
    }
    
    public boolean isBeta() {
        return (boolean) getStats().getValue("prealpha");
    }
    
    public void setBeta(boolean beta) {
        getStats().change("beta", beta, StatOperator.SET);
    }
    
    public PlayerStats getStats() {
        return playerStats;
    }
    
    public Set<String> getUnlockedTags() {
        return (Set<String>) getStats().getValue("unlockedtags");
    }
    
    public boolean isTagUnlocked(String tag) {
        return getUnlockedTags().contains(tag.toLowerCase());
    }
    
    public void addCredits(int credits) {
        getStats().change("credits", credits, StatOperator.ADD);
    }
    
    public boolean isPrivateAlpha() {
        return (boolean) getStats().getValue("privatealpha");
    }
    
    public void setPrivateAlpha(boolean value) {
        getStats().change("privatealpha", value, StatOperator.SET);
    }
    
    public void unlockTag(String tag) {
        getUnlockedTags().add(tag.toLowerCase());
    }
    
    public void lockTag(String tag) {
        getUnlockedTags().remove(tag.toLowerCase());
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
    
    public PlayerRanks getRanks() {
        if (playerRanks.getUniqueId() == null) {
            playerRanks.setUniqueId(this.uniqueId);
        }
        return playerRanks;
    }
    
    public boolean isOnline() {
        return (boolean) getStats().getValue("online");
    }
    
    public void setOnline(boolean online) {
        getStats().change("online", online, StatOperator.SET);
    }
    
    public boolean isVanish() {
        return getToggles().getValue("vanish");
    }

    public void setVanish(boolean vanish) {
        getToggles().setValue("vanish", vanish);
    }

    public boolean isIncognito() {
        return getToggles().getValue("incognito");
    }

    public void setIncognito(boolean incognito) {
        getToggles().setValue("incognito", incognito);
    }
    
    public boolean isFly() {
        return getToggles().getValue("fly");
    }
    
    public void setFly(boolean fly) {
        getToggles().setValue("fly", fly);
    }
    
    public String getServer() {
        return (String) getStats().getValue("server");
    }
    
    public void setServer(String server) {
        getStats().change("server", server, StatOperator.SET);
    }
    
    public PlayerToggles getToggles() {
        return playerToggles;
    }
}
