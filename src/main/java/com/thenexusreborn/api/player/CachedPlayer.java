package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayer extends NexusProfile {
    
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
        this.playerRanks = nexusPlayer.getRanks();
        this.playerStats = nexusPlayer.getStats();
        this.playerToggles = nexusPlayer.getToggles();
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
