package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.MojangHelper;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public abstract class PlayerManager {
    
    public static final Set<UUID> NEXUS_TEAM = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc"),
            UUID.fromString("fc6a3e38-c1c0-40a6-b7b9-152ffdadc053"), UUID.fromString("84c55f0c-2f09-4cf6-9924-57f536eb2228"))));
    
    protected Map<UUID, NexusPlayer> players = new HashMap<>();
    
    public PlayerManager() {
        
    }
    
    public Map<UUID, NexusPlayer> getPlayers() {
        return players;
    }
    
    public boolean hasData(UUID uuid) {
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            try (Statement queryStatement = connection.createStatement()) {
                ResultSet resultSet = queryStatement.executeQuery("SELECT * FROM players where uuid='" + uuid.toString() + "';");
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
                    nexusPlayer = NexusAPI.getApi().getDataManager().loadPlayer(uniqueId);
                } else {
                    String name = MojangHelper.getNameFromUUID(uniqueId);
                    try {
                        nexusPlayer = createPlayerData(uniqueId, name);
                    } catch (Exception e) {
                        return;
                    }
                }
                NexusAPI.getApi().getThreadFactory().runSync(() -> {
                    players.put(nexusPlayer.getUniqueId(), nexusPlayer);
                    action.accept(nexusPlayer);
                });
            });
        }
    }
    
    public void getNexusPlayerAsync(String name, Consumer<NexusPlayer> action) {
        for (NexusPlayer player : players.values()) {
            if (player.getName().equalsIgnoreCase(name)) {
                action.accept(player);
                return;
            }
        }
        
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
                ResultSet nameSet = statement.executeQuery("select uuid from players where lastKnownName='" + name + "';");
                if (nameSet.next()) {
                    getNexusPlayerAsync(UUID.fromString(nameSet.getString("uuid")), action);
                    return;
                } 
                
                UUID uuid = MojangHelper.getUUIDFromName(name);
                if (uuid == null) {
                    return;
                }
                
                ResultSet uuidSet = statement.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
                if (uuidSet.next()) {
                    getNexusPlayerAsync(uuid, action);
                    return;
                }
    
                try {
                    NexusPlayer player = createPlayerData(uuid, name);
                    NexusAPI.getApi().getThreadFactory().runSync(() -> {
                        players.put(uuid, player);
                        action.accept(player);
                    });
                } catch (Exception e) {
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void updateNexusTeamRank(NexusPlayer nexusPlayer) {
        if (NEXUS_TEAM.contains(nexusPlayer.getUniqueId()) && nexusPlayer.getRank() != Rank.NEXUS) {
            nexusPlayer.setRank(Rank.NEXUS, -1);
        }
        
        if (!NEXUS_TEAM.contains(nexusPlayer.getUniqueId()) && nexusPlayer.getRank() == Rank.NEXUS) {
            nexusPlayer.setRank(Rank.MEMBER, -1);
        }
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
}
