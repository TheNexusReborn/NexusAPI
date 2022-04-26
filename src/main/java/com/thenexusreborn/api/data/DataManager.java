package com.thenexusreborn.api.data;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.util.Operator;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class DataManager {
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
                Map<UUID, NexusPlayer> players = new HashMap<>();
                ResultSet resultSet = statement.executeQuery("select uuid from players;");
                while (resultSet.next()) {
                    String rawuuid = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(rawuuid);
                    NexusPlayer nexusPlayer = NexusAPI.getApi().getDataManager().loadPlayer(uuid);
                    players.put(nexusPlayer.getUniqueId(), nexusPlayer);
                }
            
                if (version == 2) {
                    statement.execute("alter table players add column tag VARCHAR(30) after lastKnownName;");
                    statement.execute("alter table players add column lastLogout varchar(100) after lastLogin");
                }
            
                for (NexusPlayer player : players.values()) {
                    NexusAPI.getApi().getDataManager().pushPlayer(player);
                }
            
                players.clear();
            }
        }
    }
    
    public <T extends Number> void pushStatChangeAsync(StatChange<T> statChange) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            try (Connection connection = NexusAPI.getApi().getConnection(); PreparedStatement statement = connection.prepareStatement("insert into statchanges(uuid, statName, value, operator, timestamp) values (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, statChange.getUuid().toString());
                statement.setString(2, statChange.getStatName());
                statement.setString(3, statChange.getValue().toString());
                statement.setString(4, statChange.getOperator().name());
                statement.setString(5, statChange.getTimestamp() + "");
                statement.executeUpdate();
        
                ResultSet generatedKeys = statement.getGeneratedKeys();
                generatedKeys.next();
                int key = generatedKeys.getInt(1);
                statChange.setId(key);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void pushStatAsync(Stat<?> stat) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            try (Connection connection = NexusAPI.getApi().getConnection()) {
                if (stat.getId() > 0) {
                    try (Statement statement = connection.createStatement()) {
                        ResultSet resultSet = statement.executeQuery("select * from stats where id='" + stat.getId() + "';");
                        if (resultSet.next()) {
                            try (PreparedStatement preparedStatement = connection.prepareStatement("update stats set value=?, modified=? where id='" +stat.getId() + "'")) {
                                preparedStatement.setString(1, stat.getValue().toString());
                                preparedStatement.setString(2, stat.getModified() + "");
                                preparedStatement.executeUpdate();
                                return;
                            }
                        }
                    }
                }
        
                try (PreparedStatement statement = connection.prepareStatement("insert into stats(uuid, name, value, created, modified) values (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, stat.getUuid().toString());
                    statement.setString(2, stat.getName());
                    statement.setString(3, stat.getValue().toString());
                    statement.setString(4, stat.getCreated() + "");
                    statement.setString(5, stat.getModified() + "");
                    statement.executeUpdate();
            
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    generatedKeys.next();
                    stat.setId(generatedKeys.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void removeStatChangeAsync(StatChange<?> statChange) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("delete from statchanges where id='" + statChange.getId() + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void pushPlayer(NexusPlayer player) {
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            boolean exists = false;
            try (Statement queryStatement = connection.createStatement()) {
                ResultSet existingResultSet = queryStatement.executeQuery("SELECT * FROM players WHERE uuid='" + player.getUniqueId() + "';");
                StringBuilder sb = new StringBuilder();
                for (Entry<Rank, Long> entry : player.getRanks().entrySet()) {
                    sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(",");
                }
            
                String ranks = sb.substring(0, sb.toString().length() - 1);
                String sql;
                if (!existingResultSet.next()) {
                    sql = "INSERT INTO players(version, uuid, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, ranks, tag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                } else {
                    sql = "UPDATE players SET version=?, uuid=?, firstJoined=?, lastLogin=?, lastLogout=?, playtime=?, lastKnownName=?, ranks=?, tag=? WHERE uuid='" + player.getUniqueId() + "';";
                }
            
                try (PreparedStatement insertStatement = connection.prepareStatement(sql)) {
                    insertStatement.setString(1, NexusPlayer.version + "");
                    insertStatement.setString(2, player.getUniqueId().toString());
                    insertStatement.setString(3, player.getFirstJoined() + "");
                    insertStatement.setString(4, player.getLastLogin() + "");
                    insertStatement.setString(5, player.getLastLogout() + "");
                    insertStatement.setString(6, player.getPlayTime() + "");
                    insertStatement.setString(7, player.getLastKnownName());
                    insertStatement.setString(8, ranks);
                    if (player.getTag() != null) {
                        insertStatement.setString(9, player.getTag().getName());
                    } else {
                        insertStatement.setString(9, "null");
                    }
                    insertStatement.execute();
                }
            
                for (Stat<?> stat : player.getStats().values()) {
                    String statSql;
                    if (stat.getId() > 0) {
                        try (PreparedStatement statement = connection.prepareStatement("update stats set value=?, modified=? where id='" + stat.getId() + "'")) {
                            statement.setString(1, stat.getValue().toString());
                            statement.setString(2, stat.getModified() + "");
                            statement.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement statement = connection.prepareStatement("insert into stats(uuid, name, value, created, modified) values(?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
                            statement.setString(1, player.getUniqueId().toString());
                            statement.setString(2, stat.getName());
                            statement.setString(3, stat.getValue().toString());
                            statement.setString(4, stat.getCreated() + "");
                            statement.setString(5, stat.getModified() + "");
                            statement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void pushPlayerAsync(NexusPlayer player) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> pushPlayer(player));
    }
    
    public NexusPlayer loadPlayer(UUID uuid) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet playerResultSet = statement.executeQuery("SELECT * FROM players WHERE uuid='" + uuid.toString() + "';");
            if (playerResultSet.next()) {
                int version = Integer.parseInt(playerResultSet.getString("version"));
                long firstJoined = 0, lastLogin = 0, lastLogout = 0, playtime = 0;
                String lastKnownName = "", rawRanks;
                Map<Rank, Long> ranks = new TreeMap<>();
                Tag tag = null;
                if (version >= 2) {
                    firstJoined = Long.parseLong(playerResultSet.getString("firstJoined"));
                    lastLogin = Long.parseLong(playerResultSet.getString("lastLogin"));
                    playtime = Long.parseLong(playerResultSet.getString("playtime"));
                    lastKnownName = playerResultSet.getString("lastKnownName");
                    rawRanks = playerResultSet.getString("ranks");
                    if (rawRanks.contains(",")) {
                        String[] rankList = rawRanks.split(",");
                        for (String rl : rankList) {
                            String[] rankSplit = rl.split("=");
                            ranks.put(Rank.valueOf(rankSplit[0]), Long.parseLong(rankSplit[1]));
                        }
                    } else {
                        String[] rankSplit = rawRanks.split("=");
                        ranks.put(Rank.valueOf(rankSplit[0]), Long.parseLong(rankSplit[1]));
                    }
                }
            
                if (version >= 3) {
                    tag = NexusAPI.getApi().getTagManager().getTag(playerResultSet.getString("tag"));
                    lastLogout = Long.parseLong(playerResultSet.getString("lastLogout"));
                }
            
                NexusPlayer nexusPlayer = NexusAPI.getApi().getPlayerFactory().createPlayer(uuid, ranks, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, tag);
            
                ResultSet statsResultSet = statement.executeQuery("select * from stats where '" + uuid + "';");
                while (statsResultSet.next()) {
                    int id = statsResultSet.getInt("id");
                    String name = statsResultSet.getString("name");
                    String rawValue = statsResultSet.getString("value");
                    long created = Long.parseLong(statsResultSet.getString("created"));
                    long modified = Long.parseLong(statsResultSet.getString("modified"));
                
                    if (!StatRegistry.isValidStat(name)) {
                        continue;
                    }
                
                    Stat<? extends Number> stat;
                    if (StatRegistry.isIntegerStat(name)) {
                        stat = StatRegistry.instantiateIntegerStat(id, name, uuid, Integer.parseInt(rawValue), created, modified);
                    } else if (StatRegistry.isDoubleStat(name)) {
                        stat = StatRegistry.instantiateDoubleStat(id, name, uuid, Double.parseDouble(rawValue), created, modified);
                    } else {
                        continue;
                    }
                
                    nexusPlayer.addStat((Stat<Number>) stat);
                }
            
                ResultSet statChangesResultSet = statement.executeQuery("select * from statchanges where '" + uuid + "'");
                while (statChangesResultSet.next()) {
                    int id = statChangesResultSet.getInt("id");
                    String name = statChangesResultSet.getString("statName");
                    String rawValue = statChangesResultSet.getString("value");
                    Operator operator = Operator.valueOf(statChangesResultSet.getString("operator"));
                    long timestamp = Long.parseLong(statChangesResultSet.getString("timestamp"));
                    Number value;
                    if (StatRegistry.isIntegerStat(name)) {
                        value = Integer.parseInt(rawValue);
                    } else if (StatRegistry.isDoubleStat(name)) {
                        value = Double.parseDouble(rawValue);
                    } else {
                        continue;
                    }
                
                    StatChange<Number> statChange = new StatChange<>(id, uuid, name, value, operator, timestamp);
                    nexusPlayer.addStatChange(statChange);
                }
                return nexusPlayer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void loadPlayerAsync(UUID uuid, Consumer<NexusPlayer> consumer) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            NexusPlayer nexusPlayer = loadPlayer(uuid);
            if (nexusPlayer != null) {
                NexusAPI.getApi().getThreadFactory().runSync(() -> consumer.accept(nexusPlayer));
            }
        });
    }
}
