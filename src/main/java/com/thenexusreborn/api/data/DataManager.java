package com.thenexusreborn.api.data;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.codec.RanksCodec;
import com.thenexusreborn.api.gamearchive.GameInfo;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.server.ServerInfo;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tournament.Tournament;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"DeprecatedIsStillUsed"})
public class DataManager {
    
    @Deprecated
    public Tournament getTournament(long id) {
        try {
            return NexusAPI.getApi().getPrimaryDatabase().get(Tournament.class, "id", id).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public List<Tournament> getTournaments() {
        List<Tournament> tournaments = null;
        try {
            tournaments = NexusAPI.getApi().getPrimaryDatabase().get(Tournament.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return tournaments;
    }
    
    @Deprecated
    public NexusPlayer loadPlayer(String name) {
        for (CachedPlayer cachedPlayer : NexusAPI.getApi().getPlayerManager().getCachedPlayers().values()) {
            if (cachedPlayer.getName().equalsIgnoreCase(name)) {
                return cachedPlayer.loadFully();
            }
        }
        return null;
    }
    
    @Deprecated
    public GameInfo getGameInfo(long id) {
        try {
            return NexusAPI.getApi().getPrimaryDatabase().get(GameInfo.class, "id", id).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public void pushGameInfo(GameInfo gameInfo) {
        NexusAPI.getApi().getPrimaryDatabase().push(gameInfo);
    }
    
    @Deprecated
    public void pushGameInfoAsync(GameInfo gameInfo, Consumer<GameInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            pushGameInfo(gameInfo);
            if (action != null) {
                action.accept(gameInfo);
            }
        });
    }
    
    @Deprecated
    public void getGameInfoAsync(int id, Consumer<GameInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            GameInfo gameInfo = getGameInfo(id);
            if (action != null) {
                action.accept(gameInfo);
            }
        });
    }
    
    @Deprecated
    public void updateAllServers(List<ServerInfo> servers) {
        if (servers == null || servers.isEmpty()) {
            return;
        }
        
        for (ServerInfo server : servers) {
            updateServerInfo(server);
        }
    }
    
    @Deprecated
    public void updateServerInfo(ServerInfo serverInfo) {
        ServerInfo infoFromDatabase = getServerInfo(serverInfo.getMulticraftId());
        if (infoFromDatabase != null) {
            serverInfo.setHiddenPlayers(infoFromDatabase.getHiddenPlayers());
            serverInfo.setMaxPlayers(infoFromDatabase.getMaxPlayers());
            serverInfo.setPlayers(infoFromDatabase.getPlayers());
            serverInfo.setState(infoFromDatabase.getState());
            serverInfo.setStatus(infoFromDatabase.getStatus());
            serverInfo.setType(infoFromDatabase.getType());
        }
    }
    
    @Deprecated
    public List<ServerInfo> getAllServers() {
        try {
            return NexusAPI.getApi().getPrimaryDatabase().get(ServerInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Deprecated
    public void getAllServersAsync(Consumer<List<ServerInfo>> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            List<ServerInfo> allServers = getAllServers();
            if (allServers != null && !allServers.isEmpty()) {
                action.accept(allServers);
            }
        });
    }
    
    @Deprecated
    public ServerInfo getServerInfo(long multicraftId) {
        try {
            List<ServerInfo> servers = NexusAPI.getApi().getPrimaryDatabase().get(ServerInfo.class, "multicraftId", multicraftId);
            if (servers != null) {
                return servers.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Deprecated
    public void getServerInfoAsync(int multicraftId, Consumer<ServerInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            ServerInfo serverInfo = getServerInfo(multicraftId);
            if (serverInfo != null) {
                action.accept(serverInfo);
            }
        });
    }
    
    @Deprecated
    public void pushServerInfo(ServerInfo serverInfo) {
        NexusAPI.getApi().getPrimaryDatabase().push(serverInfo);
    }
    
    @Deprecated
    public void pushServerInfoAsync(ServerInfo serverInfo) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> pushServerInfo(serverInfo));
    }
    
    @Deprecated
    public <T extends Number> void pushStatChangeAsync(StatChange statChange) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> NexusAPI.getApi().getPrimaryDatabase().push(statChange));
    }
    
    @Deprecated
    public void pushStatAsync(Stat stat) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> NexusAPI.getApi().getPrimaryDatabase().push(stat));
    }
    
    @Deprecated
    public void removeStatChangeAsync(StatChange statChange) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> NexusAPI.getApi().getPrimaryDatabase().delete(StatChange.class, statChange.getId()));
    }
    
    @Deprecated
    public void pushPlayer(NexusPlayer player) {
        NexusAPI.getApi().getPrimaryDatabase().push(player);
    }
    
    @Deprecated
    public void pushPlayerPreferences(NexusPlayer player) {
        for (Preference preference : player.getPreferences().values()) {
            NexusAPI.getApi().getPrimaryDatabase().push(preference);
        }
    }
    
    @Deprecated
    public void pushPlayerAsync(NexusPlayer player) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> pushPlayer(player));
    }
    
    @Deprecated
    public void refreshPlayerStats(NexusPlayer nexusPlayer) {
        try {
            List<Stat> stats = NexusAPI.getApi().getPrimaryDatabase().get(Stat.class, "uuid", nexusPlayer.getUniqueId());
            for (Stat stat : stats) {
                nexusPlayer.changeStat(stat.getName(), stat.getValue(), StatOperator.SET);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Deprecated
    public NexusPlayer loadPlayer(UUID uuid) {
        return NexusAPI.getApi().getPlayerManager().getCachedPlayers().get(uuid).loadFully();
    }
    
    @Deprecated
    public Map<Rank, Long> parseRanks(String rawRanks) {
        return new RanksCodec().decode(rawRanks);
    }
    
    @Deprecated
    public void loadPlayerAsync(UUID uuid, Consumer<NexusPlayer> consumer) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            NexusPlayer nexusPlayer = loadPlayer(uuid);
            if (nexusPlayer != null) {
                NexusAPI.getApi().getThreadFactory().runSync(() -> consumer.accept(nexusPlayer));
            }
        });
    }
    
    @Deprecated
    public void pushPunishment(Punishment punishment) {
        NexusAPI.getApi().getPrimaryDatabase().push(punishment);
    }
    
    @Deprecated
    public Punishment getPunishment(long id) {
        try {
            return NexusAPI.getApi().getPrimaryDatabase().get(Punishment.class, "id", id).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public Set<UUID> getPlayersByIp(String ip) {
        Set<IPEntry> allIps = new HashSet<>();
        Set<UUID> players = new HashSet<>();
        for (IPEntry ipEntry : NexusAPI.getApi().getPlayerManager().getIpHistory()) {
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
    public void addIpHistory(UUID uuid, String ip) {
        CachedPlayer cachedPlayer = NexusAPI.getApi().getPlayerManager().getCachedPlayers().get(uuid);
        for (IPEntry ipEntry : cachedPlayer.getIpHistory()) {
            if (ipEntry.getIp().equalsIgnoreCase(ip)) {
                return;
            }
        }
        
        IPEntry ipEntry = new IPEntry(ip, uuid);
        NexusAPI.getApi().getPrimaryDatabase().push(ipEntry);
        NexusAPI.getApi().getPlayerManager().getIpHistory().add(ipEntry);
        cachedPlayer.getIpHistory().add(ipEntry);
    }
    
    @Deprecated
    public Set<IPEntry> getIpHistory() {
        return NexusAPI.getApi().getPlayerManager().getIpHistory();
    }
    
    @Deprecated
    public List<Punishment> getPunishments() {
        try {
            return NexusAPI.getApi().getPrimaryDatabase().get(Punishment.class);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Deprecated
    public void pushTournament(Tournament tournament) {
        NexusAPI.getApi().getPrimaryDatabase().push(tournament);
    }
}
