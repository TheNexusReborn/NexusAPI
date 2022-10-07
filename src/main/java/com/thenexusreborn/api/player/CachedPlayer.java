package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayer extends NexusProfile {
    
    private CachedPlayer() {
        this((UUID) null);
    }
    
    public CachedPlayer(UUID uniqueId) {
        this(uniqueId, "");
    }
    
    public CachedPlayer(UUID uniqueId, String name) {
        this(0, uniqueId, name);
    }
    
    public CachedPlayer(long id, UUID uniqueId, String name) {
        super(id, uniqueId, name);
    }
    
    public CachedPlayer(NexusPlayer nexusPlayer) {
        this(nexusPlayer.id, nexusPlayer.uniqueId, nexusPlayer.name);
        this.ipHistory = nexusPlayer.ipHistory;
        this.ranks = nexusPlayer.getRanks();
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
