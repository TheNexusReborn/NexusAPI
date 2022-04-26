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
    
    public void setupMysql() throws SQLException {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players(version varchar(10), uuid varchar(36) NOT NULL, firstJoined varchar(100), lastLogin varchar(100), lastLogout varchar(100), playtime varchar(100), lastKnownName varchar(16), tag varchar(30), ranks varchar(10000));");
            statement.execute("CREATE TABLE IF NOT EXISTS stats(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), name varchar(100), value varchar(1000), created varchar(100), modified varchar(100));");
            statement.execute("CREATE TABLE IF NOT EXISTS statchanges(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), statName varchar(100), value varchar(100), operator varchar(50), timestamp varchar(100));");
            
            int version = 0;
            boolean convert = false;
            ResultSet versionSet = statement.executeQuery("select version from players;");
            while (versionSet.next()) {
                int v = Integer.parseInt(versionSet.getString("version"));
                if (v < NexusPlayer.version) {
                    version = v;
                    convert = true;
                    break;
                }
            }
            
            if (convert) {
                ResultSet resultSet = statement.executeQuery("select uuid from players;");
                while (resultSet.next()) {
                    String rawuuid = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(rawuuid);
                    NexusPlayer nexusPlayer = NexusAPI.getApi().getDataManager().loadPlayer(uuid);
                }
                
                if (version == 2) {
                    statement.execute("alter table players add column tag VARCHAR(30) after lastKnownName;");
                    statement.execute("alter table players add column lastLogout varchar(100) after lastLogin");
                }
    
                for (NexusPlayer player : this.players.values()) {
                    NexusAPI.getApi().getDataManager().pushPlayer(player);
                }
                
                this.players.clear();
            }
        }
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
                    nexusPlayer = NexusAPI.getApi().getPlayerFactory().createPlayer(uniqueId, name);
                    NexusAPI.getApi().getDataManager().pushPlayerAsync(nexusPlayer);
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
    
                NexusPlayer player = NexusAPI.getApi().getPlayerFactory().createPlayer(uuid, name);
                NexusAPI.getApi().getDataManager().pushPlayer(player);
                NexusAPI.getApi().getThreadFactory().runSync(() -> {
                    players.put(uuid, player);
                    action.accept(player);
                });
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
