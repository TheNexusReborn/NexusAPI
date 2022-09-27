package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.stats.StatChange;
import com.thenexusreborn.api.stats.StatHelper;
import com.thenexusreborn.api.stats.StatOperator;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayer implements NexusProfile {
    protected long id;
    protected UUID uniqueId;
    protected String name;
    protected Set<IPEntry> ipHistory = new HashSet<>();
    protected long lastLogout;
    protected boolean online, vanish, incognito, fly;
    protected String server;
    protected Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
    protected Set<String> unlockedTags = new HashSet<>();
    private boolean privateAlpha;
    
    private CachedPlayer() {
    }
    
    public CachedPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public CachedPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }
    
    public CachedPlayer(long id, UUID uniqueId, String name) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.name = name;
    }
    
    public CachedPlayer(NexusPlayer nexusPlayer) {
        this.id = nexusPlayer.id;
        this.uniqueId = nexusPlayer.uniqueId;
        this.name = nexusPlayer.name;
        this.ipHistory = nexusPlayer.ipHistory;
        this.lastLogout = (long) nexusPlayer.getStatValue("lastlogout");
        this.online = (boolean) nexusPlayer.getStatValue("online");
        this.vanish = nexusPlayer.getPreferenceValue("vanish");
        this.incognito = nexusPlayer.getPreferenceValue("incognito");
        this.server = (String) nexusPlayer.getStatValue("server");
        this.ranks = nexusPlayer.getRanks();
        this.privateAlpha = nexusPlayer.isPrivateAlpha();
    }
    
    @Override
    public long getLastLogout() {
        return lastLogout;
    }
    
    @Override
    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }
    
    @Override
    public boolean isOnline() {
        return online;
    }
    
    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    @Override
    public boolean isVanish() {
        return vanish;
    }
    
    @Override
    public void setVanish(boolean vanish) {
        this.vanish = vanish;
    }
    
    @Override
    public boolean isIncognito() {
        return incognito;
    }
    
    @Override
    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }
    
    @Override
    public String getServer() {
        return server;
    }
    
    @Override
    public void setServer(String server) {
        this.server = server;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
    
    @Override
    public NexusPlayer loadFully() {
        try {
            List<NexusPlayer> players = NexusAPI.getApi().getPrimaryDatabase().get(NexusPlayer.class, "id", this.id);
            if (!players.isEmpty()) {
                return players.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Rank, Long> getRanks() {
        return ranks;
    }

    @Override
    public Rank getRank() {
        return Rank.getPrimaryRank(this.uniqueId, ranks);
    }

    @Override
    public void addRank(Rank rank, long expire) {
        this.ranks.put(rank, expire);
    }

    @Override
    public void removeRank(Rank rank) {
        if (rank != Rank.NEXUS) {
            this.ranks.remove(rank);
        }
    }

    @Override
    public void setRank(Rank rank, long expire) {
        this.ranks.clear();
        this.ranks.put(rank, expire);
    }

    @Override
    public void setFly(boolean value) {
        this.fly = value;
    }
    
    @Override
    public boolean isFly() {
        return fly;
    }

    @Override
    public Set<String> getUnlockedTags() {
        return unlockedTags;
    }

    @Override
    public void unlockTag(String tag) {
        this.unlockedTags.add(tag.toLowerCase());
    }

    @Override
    public void lockTag(String tag) {
        this.unlockedTags.remove(tag.toLowerCase());
    }

    public void setUnlockedTags(Set<String> unlockedTags) {
        this.unlockedTags.clear();
        this.unlockedTags.addAll(unlockedTags);
    }

    @Override
    public boolean isTagUnlocked(String tag) {
        return false;
    }

    @Override
    public void addCredits(int credits) {
        StatChange statChange = new StatChange(StatHelper.getInfo("credits"), this.uniqueId, credits, StatOperator.ADD, System.currentTimeMillis());
        NexusAPI.getApi().getPrimaryDatabase().push(statChange);
    }
    
    @Override
    public boolean isPrivateAlpha() {
        return privateAlpha;
    }
    
    @Override
    public void setPrivateAlpha(boolean value) {
        this.privateAlpha = value;
    }
    
    public void setRanks(Map<Rank, Long> ranks) {
        this.ranks = ranks;
    }
}
