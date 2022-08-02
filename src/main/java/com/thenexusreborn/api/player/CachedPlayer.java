package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayer {
    protected long id;
    protected UUID uniqueId;
    protected String name;
    protected Set<IPEntry> ipHistory = new HashSet<>();
    protected long lastLogout;
    protected boolean online, vanish, incognito;
    protected String server;
    
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
    }
    
    public long getLastLogout() {
        return lastLogout;
    }
    
    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    public boolean isVanish() {
        return vanish;
    }
    
    public void setVanish(boolean vanish) {
        this.vanish = vanish;
    }
    
    public boolean isIncognito() {
        return incognito;
    }
    
    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public long getId() {
        return id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
    
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
}
