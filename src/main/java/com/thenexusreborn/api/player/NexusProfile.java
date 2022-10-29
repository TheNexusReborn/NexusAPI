package com.thenexusreborn.api.player;

import com.thenexusreborn.api.storage.annotations.*;
import com.thenexusreborn.api.storage.codec.RanksCodec;
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
    protected PlayerRanks ranks;
    @ColumnIgnored
    protected PlayerStats playerStats;
    @ColumnIgnored
    protected PlayerToggles playerToggles;
    
    protected NexusProfile() {
        this(null);
    }
    
    public NexusProfile(UUID uniqueId) {
        this(0, uniqueId, "");
    }
    
    public NexusProfile(long id, UUID uniqueId, String name) {
        this.id = id;
        this.name = name;
        this.uniqueId = uniqueId;
        this.playerToggles = new PlayerToggles();
        this.playerStats = new PlayerStats(uniqueId);
        this.ranks = new PlayerRanks(uniqueId);
    }
    
    public long getFirstJoined() {
        return getStats().getValue("firstjoined").getAsLong();
    }
    
    public void setFirstJoined(long firstJoined) {
        getStats().change("firstjoined", firstJoined, StatOperator.SET);
    }
    
    public long getLastLogin() {
        return getStats().getValue("lastlogin").getAsLong();
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
        return getStats().getValue("lastlogout").getAsLong();
    }
    
    public void setLastLogout(long lastLogout) {
        getStats().change("lastlogout", lastLogout, StatOperator.SET);
    }
    
    public boolean isPrealpha() {
        return getStats().getValue("prealpha").getAsBoolean();
    }
    
    public void setPrealpha(boolean prealpha) {
        getStats().change("prealpha", prealpha, StatOperator.SET);
    }
    
    public boolean isAlpha() {
        return getStats().getValue("alpha").getAsBoolean();
    }
    
    public void setAlpha(boolean alpha) {
        getStats().change("alpha", alpha, StatOperator.SET);
    }
    
    public boolean isBeta() {
        return getStats().getValue("beta").getAsBoolean();
    }
    
    public void setBeta(boolean beta) {
        getStats().change("beta", beta, StatOperator.SET);
    }
    
    public PlayerStats getStats() {
        if (playerStats.getUniqueId() == null) {
            playerStats.setUniqueId(uniqueId);
        }
        return playerStats;
    }
    
    public Set<String> getUnlockedTags() {
        return getStats().getValue("unlockedtags").getAsStringSet();
    }
    
    public boolean isTagUnlocked(String tag) {
        return getUnlockedTags().contains(tag.toLowerCase());
    }
    
    public void addCredits(int credits) {
        getStats().change("credits", credits, StatOperator.ADD);
    }
    
    public boolean isPrivateAlpha() {
        return getStats().getValue("privatealpha").getAsBoolean();
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
        if (ranks.getUniqueId() == null) {
            ranks.setUniqueId(this.uniqueId);
        }
        return ranks;
    }
    
    public boolean isOnline() {
        return getStats().getValue("online").getAsBoolean();
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
        return getStats().getValue("server").getAsString();
    }
    
    public void setServer(String server) {
        getStats().change("server", server, StatOperator.SET);
    }
    
    public PlayerToggles getToggles() {
        return playerToggles;
    }
    
    public String getColoredName() {
        return getRanks().get().getColor() + getName();
    }
    
    public void removeCredits(int credits) {
        getStats().change("credits", credits, StatOperator.SUBTRACT);
    }
}
