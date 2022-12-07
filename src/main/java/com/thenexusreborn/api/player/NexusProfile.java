package com.thenexusreborn.api.player;

import com.thenexusreborn.api.frameworks.value.Value;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.storage.annotations.ColumnIgnored;
import com.thenexusreborn.api.storage.annotations.ColumnInfo;
import com.thenexusreborn.api.storage.annotations.Primary;
import com.thenexusreborn.api.storage.codec.RanksCodec;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    protected PlayerStats stats;
    @ColumnIgnored
    protected PlayerToggles toggles;
    @ColumnIgnored
    protected PlayerTags tags;
    
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
        this.toggles = new PlayerToggles();
        this.stats = new PlayerStats(uniqueId);
        this.ranks = new PlayerRanks(uniqueId);
        this.tags = new PlayerTags(uniqueId);
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

    public String getDisplayName() {
        if (getRank() != Rank.MEMBER) {
            return getRank().getPrefix() + " &f" + getName();
        } else {
            return getRank().getPrefix() + getName();
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
        if (stats.getUniqueId() == null) {
            stats.setUniqueId(uniqueId);
        }
        return stats;
    }
    
    public Value getStatValue(String statName) {
        return getStats().getValue(statName);
    }
    
    public void changeStat(String statName, Object value, StatOperator operator) {
        getStats().change(statName, value, operator);
    }
    
    public void addStatChange(StatChange change) {
        getStats().addChange(change);
    }
    
    public Stat getStat(String statName) {
        return getStats().get(statName);
    }
    
    public void clearStatChanges() {
        getStats().clearChanges();
    }
    
    public void addStat(Stat stat) {
        getStats().add(stat);
    }

    public void addCredits(int credits) {
        getStats().change("credits", credits, StatOperator.ADD);
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
    
    public Rank getRank() {
        return getRanks().get();
    }
    
    public void addRank(Rank rank, long expire) {
        getRanks().add(rank, expire);
    }
    
    public void setRank(Rank rank, long expire) {
        getRanks().set(rank, expire);
    }
    
    public void removeRank(Rank rank) {
        getRanks().remove(rank);
    }
    
    public boolean hasRank(Rank rank) {
        return getRanks().contains(rank);
    }
    
    public boolean isOnline() {
        return getStats().getValue("online").getAsBoolean();
    }
    
    public void setOnline(boolean online) {
        getStats().change("online", online, StatOperator.SET);
    }
    
    public String getServer() {
        return getStats().getValue("server").getAsString();
    }
    
    public void setServer(String server) {
        getStats().change("server", server, StatOperator.SET);
    }
    
    public PlayerToggles getToggles() {
        return toggles;
    }
    
    public Toggle getToggle(String toggleName) {
        return getToggles().get(toggleName);
    }
    
    public boolean getToggleValue(String toggleName) {
        return getToggles().getValue(toggleName);
    }
    
    public void setToggleValue(String toggleName, boolean value) {
        getToggles().setValue(toggleName, value);
    }
    
    public String getColoredName() {
        return getRank().getColor() + getName();
    }
    
    public void removeCredits(int credits) {
        getStats().change("credits", credits, StatOperator.SUBTRACT);
    }

    public PlayerTags getTags() {
        if (tags.getUuid() == null) {
            tags.setUuid(this.uniqueId);
        }
        return tags;
    }
    
    public void addToggle(Toggle toggle) {
        getToggles().add(toggle);
    }
}
