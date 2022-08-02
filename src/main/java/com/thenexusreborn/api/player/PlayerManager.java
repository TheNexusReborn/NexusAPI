package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.MojangHelper;

import java.util.*;
import java.util.function.Consumer;

public abstract class PlayerManager {
    
    public static final Set<UUID> NEXUS_TEAM = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc"),
            UUID.fromString("fc6a3e38-c1c0-40a6-b7b9-152ffdadc053"), UUID.fromString("84c55f0c-2f09-4cf6-9924-57f536eb2228"))));
    
    protected final Map<UUID, NexusPlayer> players = new HashMap<>();
    protected final Set<IPEntry> ipHistory = new HashSet<>();
    
    protected final Map<UUID, CachedPlayer> cachedPlayers = new HashMap<>();
    
    public Map<UUID, NexusPlayer> getPlayers() {
        return players;
    }
    
    public Map<UUID, CachedPlayer> getCachedPlayers() {
        return cachedPlayers;
    }
    
    public boolean hasData(UUID uuid) {
        return getCachedPlayers().get(uuid) != null;
    }
    
    public void saveToMySQLAsync(NexusPlayer player) {
        NexusAPI.getApi().getDataManager().pushPlayerAsync(player);
    }
    
    public abstract NexusPlayer createPlayerData(UUID uniqueId, String name);
    
    public void getNexusPlayerAsync(UUID uniqueId, Consumer<NexusPlayer> action) {
        if (players.containsKey(uniqueId)) {
            action.accept(players.get(uniqueId));
        } else {
            NexusAPI.getApi().getThreadFactory().runAsync(() -> {
                NexusPlayer nexusPlayer;
                if (hasData(uniqueId)) {
                    do {
                        nexusPlayer = NexusAPI.getApi().getDataManager().loadPlayer(uniqueId);
                    } while (nexusPlayer == null);
                } else {
                    try {
                        nexusPlayer = createPlayerData(uniqueId, MojangHelper.getNameFromUUID(uniqueId));
                    } catch (Exception e) {
                        do {
                            nexusPlayer = NexusAPI.getApi().getDataManager().loadPlayer(uniqueId);
                        } while (nexusPlayer == null);
                    }
                }
                NexusPlayer finalNexusPlayer = nexusPlayer;
                NexusAPI.getApi().getThreadFactory().runSync(() -> {
                    players.put(finalNexusPlayer.getUniqueId(), finalNexusPlayer);
                    action.accept(finalNexusPlayer);
                });
            });
        }
    }
    
    public void getNexusPlayerAsync(String name, Consumer<NexusPlayer> action) {
        for (NexusPlayer player : players.values()) {
            if (player != null) {
                if (player.getName().equalsIgnoreCase(name)) {
                    action.accept(player);
                    return;
                }
            }
        }
        
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            for (CachedPlayer cachedPlayer : getCachedPlayers().values()) {
                if (cachedPlayer.getName().equalsIgnoreCase(name)) {
                    NexusPlayer nexusPlayer = cachedPlayer.loadFully();
                    players.put(nexusPlayer.getUniqueId(), nexusPlayer);
                    action.accept(nexusPlayer);
                }
            }
        });
    }
    
    public NexusPlayer getNexusPlayer(UUID uniqueId) {
        return this.players.get(uniqueId);
    }
    
    public void saveData() {
        for (NexusPlayer nexusPlayer : this.players.values()) {
            NexusAPI.getApi().getDataManager().pushPlayer(nexusPlayer);
        }
    }
    
    public NexusPlayer getNexusPlayer(String name) {
        for (NexusPlayer nexusPlayer : this.players.values()) {
            if (nexusPlayer.getName().equalsIgnoreCase(name)) {
                return nexusPlayer;
            }
        }
        
        return null;
    }
    
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
}
