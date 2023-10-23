package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.MojangHelper;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentType;

import java.util.*;
import java.util.function.Consumer;

public abstract class PlayerManager {
    
    public static final Set<UUID> NEXUS_TEAM = Set.of(UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc"), UUID.fromString("fc6a3e38-c1c0-40a6-b7b9-152ffdadc053"), UUID.fromString("84c55f0c-2f09-4cf6-9924-57f536eb2228"));
    
    protected final Map<UUID, NexusPlayer> players = new HashMap<>();
    protected final Map<UUID, CachedPlayer> cachedPlayers = new HashMap<>();

    protected Map<UUID, Long> loginTimes = new HashMap<>();
    protected Map<UUID, Session> sessions = new HashMap<>();
    
    protected final Set<IPEntry> ipHistory = new HashSet<>();
    
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
        NexusAPI.getApi().getScheduler().runTaskAsynchronously(() -> NexusAPI.getApi().getPrimaryDatabase().saveSilent(player));
    }
    
    public NexusPlayer createPlayerData(UUID uniqueId, String name) {
        NexusPlayer nexusPlayer = new NexusPlayer(uniqueId);
        if (name != null && !name.equalsIgnoreCase("")) {
            nexusPlayer.setName(name);
        }
        nexusPlayer.setFirstJoined(System.currentTimeMillis());
        nexusPlayer.setLastLogin(System.currentTimeMillis());
        nexusPlayer.setLastLogout(System.currentTimeMillis());
        NexusAPI.getApi().getPrimaryDatabase().saveSilent(nexusPlayer);
        CachedPlayer player = new CachedPlayer(nexusPlayer);
        getCachedPlayers().put(nexusPlayer.getUniqueId(), player);
        return nexusPlayer;
    }
    
    public void handlePlayerLeave(NexusPlayer player) {
        this.cachedPlayers.put(player.getUniqueId(), new CachedPlayer(player));
    }
    
    public Set<UUID> getPlayersByIp(String ip) {
        Set<IPEntry> allIps = new HashSet<>();
        Set<UUID> players = new HashSet<>();
        for (IPEntry ipEntry : getIpHistory()) {
            if (ipEntry.getIp().equalsIgnoreCase(ip)) {
                allIps.add(ipEntry);
                players.add(ipEntry.getUuid());
            }
        }
        
        for (UUID player : players) {
            CachedPlayer cachedPlayer = NexusAPI.getApi().getPlayerManager().getCachedPlayers().get(player);
            allIps.addAll(cachedPlayer.getIpHistory());
        }
        
        for (IPEntry ipEntry : allIps) {
            players.add(ipEntry.getUuid());
        }
        
        return players;
    }
    
    @Deprecated
    public void getNexusPlayerAsync(UUID uniqueId, Consumer<NexusPlayer> action) {
        if (players.containsKey(uniqueId)) {
            action.accept(players.get(uniqueId));
        } else {
            NexusAPI.getApi().getScheduler().runTaskAsynchronously(() -> {
                NexusPlayer nexusPlayer = null;
                if (hasData(uniqueId)) {
                    do {
                        try {
                            nexusPlayer = NexusAPI.getApi().getPrimaryDatabase().get(NexusPlayer.class, "uniqueId", uniqueId).get(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (nexusPlayer == null);
                } else {
                    try {
                        nexusPlayer = createPlayerData(uniqueId, MojangHelper.getNameFromUUID(uniqueId));
                    } catch (Exception e) {
                        do {
                            try {
                                nexusPlayer = NexusAPI.getApi().getPrimaryDatabase().get(NexusPlayer.class, "uniqueId", uniqueId).get(0);
                            } catch (Exception ex) {
                            }
                        } while (nexusPlayer == null);
                    }
                }
                NexusPlayer finalNexusPlayer = nexusPlayer;
                NexusAPI.getApi().getScheduler().runTask(() -> {
                    players.put(finalNexusPlayer.getUniqueId(), finalNexusPlayer);
                    action.accept(finalNexusPlayer);
                });
            });
        }
    }

    public NexusProfile getProfile(UUID uuid) {
        if (this.players.containsKey(uuid)) {
            return this.players.get(uuid);
        } else {
            return this.cachedPlayers.get(uuid);
        }
    }

    public NexusProfile getProfile(String name) {
        for (NexusPlayer player : new ArrayList<>(this.players.values())) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }

        for (CachedPlayer player : new ArrayList<>(this.cachedPlayers.values())) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }

        return null;
    }
    
    @Deprecated
    public void getNexusPlayerAsync(String name, Consumer<NexusPlayer> action) {
        for (NexusPlayer player : players.values()) {
            if (player != null) {
                if (player.getName().equalsIgnoreCase(name)) {
                    action.accept(player);
                    return;
                }
            }
        }
        
        NexusAPI.getApi().getScheduler().runTaskAsynchronously(() -> {
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
            NexusAPI.getApi().getPrimaryDatabase().saveSilent(nexusPlayer);
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
    
    public void addIpHistory(UUID uniqueId, String hostName) {
        CachedPlayer cachedPlayer = NexusAPI.getApi().getPlayerManager().getCachedPlayers().get(uniqueId);
        for (IPEntry ipEntry : cachedPlayer.getIpHistory()) {
            if (ipEntry.getIp().equalsIgnoreCase(hostName)) {
                return;
            }
        }
    
        IPEntry ipEntry = new IPEntry(hostName, uniqueId);
        NexusAPI.getApi().getPrimaryDatabase().saveSilent(ipEntry);
        NexusAPI.getApi().getPlayerManager().getIpHistory().add(ipEntry);
        cachedPlayer.getIpHistory().add(ipEntry);
    }
    
    public CachedPlayer getCachedPlayer(String name) {
        for (CachedPlayer cachedPlayer : new ArrayList<>(this.cachedPlayers.values())) {
            if (cachedPlayer.getName().equalsIgnoreCase(name)) {
                return cachedPlayer;
            }
        }
        return null;
    }
    
    public CachedPlayer getCachedPlayer(UUID uuid) {
        return this.cachedPlayers.get(uuid);
    }
    
    public NexusPlayer getOrLoadNexusPlayer(UUID uuid) {
        NexusPlayer player = getNexusPlayer(uuid);
        if (player != null) {
            return player;
        }
        
        return getCachedPlayer(uuid).loadFully();
    }

    protected Punishment checkPunishments(UUID uniqueId) {
        List<Punishment> punishments = NexusAPI.getApi().getPunishmentManager().getPunishmentsByTarget(uniqueId);
        if (!punishments.isEmpty()) {
            for (Punishment punishment : punishments) {
                if (punishment.getType() == PunishmentType.BAN || punishment.getType() == PunishmentType.BLACKLIST) {
                    if (punishment.isActive()) {
                        if (!PlayerManager.NEXUS_TEAM.contains(uniqueId)) {
                            return punishment;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Session getSession(UUID uniqueId) {
        return this.sessions.get(uniqueId);
    }
}
