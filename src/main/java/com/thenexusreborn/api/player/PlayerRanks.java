package com.thenexusreborn.api.player;

import java.util.*;

public class PlayerRanks {
    private UUID uniqueId;
    protected Map<Rank, Long> ranks = new HashMap<>();
    
    public PlayerRanks(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public Rank get() {
        if (PlayerManager.NEXUS_TEAM.contains(uniqueId)) {
            return Rank.NEXUS;
        }
    
        for (Map.Entry<Rank, Long> entry : new EnumMap<>(ranks).entrySet()) {
            if (entry.getValue() == -1) {
                return entry.getKey();
            }
        
            if (System.currentTimeMillis() <= entry.getValue()) {
                return entry.getKey();
            }
        }
    
        return Rank.MEMBER;
    }
    
    public void add(Rank rank, long expire) {
        this.ranks.put(rank, expire);
    }
    
    public void set(Rank rank, long expire) {
        if (this.ranks.containsKey(Rank.NEXUS)) {
            return;
        }
        
        this.ranks.clear();
        this.ranks.put(rank, expire);
    }
    
    public void remove(Rank rank) {
        if (rank == Rank.NEXUS) {
            return;
        }
        
        this.ranks.remove(rank);
    }
    
    public Map<Rank, Long> findAll() {
        return new HashMap<>(this.ranks);
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public boolean contains(Rank rank) {
        return this.ranks.containsKey(rank);
    }
    
    public long getExpire(Rank rank) {
        return this.ranks.get(rank);
    }
    
    public void setAll(PlayerRanks ranks) {
        this.ranks.clear();
        this.ranks.putAll(ranks.findAll());
    }
}
