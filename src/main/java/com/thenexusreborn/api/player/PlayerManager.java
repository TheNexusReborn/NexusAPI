package com.thenexusreborn.api.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.stardevllc.starlib.Pair;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentType;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class PlayerManager {

    public static final Set<UUID> NEXUS_TEAM = Set.of(UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc"), UUID.fromString("fc6a3e38-c1c0-40a6-b7b9-152ffdadc053"), UUID.fromString("84c55f0c-2f09-4cf6-9924-57f536eb2228"));

    protected final Map<UUID, NexusPlayer> players = new HashMap<>(); //Online players
    protected final Cache<UUID, NexusPlayer> cachedPlayers = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    protected final BiMap<UUID, Name> uuidNameMap = HashBiMap.create();
    protected final Map<UUID, PlayerRanks> uuidRankMap = new HashMap<>();
    protected final Set<IPEntry> ipHistory = new HashSet<>();
    
    public static class Name {
        private String name;

        public Name(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Name other = (Name) o;
            return this.name.equalsIgnoreCase(other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name.toLowerCase());
        }
    }

    public Map<UUID, NexusPlayer> getPlayers() {
        return players;
    }

    public Cache<UUID, NexusPlayer> getCachedPlayers() {
        return cachedPlayers;
    }

    public BiMap<UUID, Name> getUuidNameMap() {
        return uuidNameMap;
    }

    public UUID getUUIDFromName(String rawName) {
        return getUuidNameMap().inverse().get(new Name(rawName));
    }

    public String getNameFromUUID(UUID uuid) {
        return this.uuidNameMap.get(uuid).name;
    }
    
    public Rank getPlayerRank(UUID uuid) {
        return this.uuidRankMap.get(uuid).get();
    }
    
    public PlayerRanks getPlayerRanks(UUID uuid) {
        return this.uuidRankMap.get(uuid);
    }
    
    public Rank getPlayerRank(String name) {
        UUID uuid = getUUIDFromName(name);
        if (uuid == null) {
            return null;
        }
        return this.uuidRankMap.get(uuid).get();
    }

    public Map<UUID, PlayerRanks> getUuidRankMap() {
        return uuidRankMap;
    }

    public Pair<UUID, String> getPlayerFromIdentifier(String identifier) {
        UUID uniqueID;
        String name;
        try {
            uniqueID = UUID.fromString(identifier);
            name = getNameFromUUID(uniqueID);
        } catch (Exception e) {
            uniqueID = getUUIDFromName(identifier);
            name = identifier;
        }
        
        if (uniqueID == null || name == null) {
            return null;
        }
        
        return new Pair<>(uniqueID, name);
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
        return nexusPlayer;
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

        for (IPEntry ipEntry : allIps) {
            players.add(ipEntry.getUuid());
        }

        return players;
    }

    public NexusPlayer getNexusPlayer(UUID uniqueId) {
        NexusPlayer player = this.players.get(uniqueId);
        if (player == null) {
            return cachedPlayers.getIfPresent(uniqueId);
        }
        return player;
    }

    public void saveData() {
        for (NexusPlayer nexusPlayer : this.players.values()) {
            NexusAPI.getApi().getPrimaryDatabase().saveSilent(nexusPlayer);
        }
    }

    public NexusPlayer getNexusPlayer(String name) {
        return getNexusPlayer(getUUIDFromName(name));
    }

    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }

    public void addIpHistory(UUID uniqueId, String hostName) {
        PlayerManager playerManager = NexusAPI.getApi().getPlayerManager();
        for (IPEntry ipEntry : playerManager.getIpHistory()) {
            if (ipEntry.getIp().equalsIgnoreCase(hostName)) {
                return;
            }
        }

        IPEntry ipEntry = new IPEntry(hostName, uniqueId);
        NexusAPI.getApi().getPrimaryDatabase().saveSilent(ipEntry);
        playerManager.getIpHistory().add(ipEntry);
    }

    public NexusPlayer getOrLoadNexusPlayer(UUID uuid) {
        NexusPlayer player = getNexusPlayer(uuid);
        if (player != null) {
            return player;
        }

        if (getUuidNameMap().containsKey(uuid)) {
            try {
                player = NexusAPI.getApi().getPrimaryDatabase().get(NexusPlayer.class, "uniqueId", uuid.toString()).get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (player == null) {
            player = createPlayerData(uuid, null);
            NexusAPI.getApi().getPrimaryDatabase().saveSilent(player);
        }

        return player;
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
}