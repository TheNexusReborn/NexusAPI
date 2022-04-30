package com.thenexusreborn.api.data;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.server.ServerInfo;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.util.Operator;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class DataManager {
    public void setupMysql() throws SQLException {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players(version varchar(10), uuid varchar(36) NOT NULL, firstJoined varchar(100), lastLogin varchar(100), lastLogout varchar(100), playtime varchar(100), lastKnownName varchar(16), tag varchar(30), ranks varchar(1000), unlockedTags varchar(1000), lastPlaytimeNotification int);");
            statement.execute("CREATE TABLE IF NOT EXISTS stats(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), name varchar(100), value varchar(1000), created varchar(100), modified varchar(100));");
            statement.execute("CREATE TABLE IF NOT EXISTS statchanges(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), statName varchar(100), value varchar(100), operator varchar(50), timestamp varchar(100));");
            statement.execute("create table if not exists serverinfo(multicraftId int primary key not null, ip varchar(50), name varchar(100), port int, players int, maxPlayers int, hiddenPlayers int, type varchar(100), status varchar(100), state varchar(100));");
            
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
                NexusAPI.getApi().getLogger().info("Converting existing player data due to a new model version.");
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
                
                if (version == 3) {
                    statement.execute("alter table players add column unlockedTags varchar(1000) after ranks;");
                }
                
                if (version == 4) {
                    statement.execute("alter table players add column lastPlaytimeNotification int");
                }
                
                for (NexusPlayer player : players.values()) {
                    NexusAPI.getApi().getDataManager().pushPlayer(player);
                }
                
                players.clear();
                NexusAPI.getApi().getLogger().info("Conversion complete");
            }
        }
    }
    
    public void updateAllServers(List<ServerInfo> servers) {
        if (servers == null || servers.isEmpty()) {
            return;
        }
        
        for (ServerInfo server : servers) {
            updateServerInfo(server);
        }
    }
    
    public void updateServerInfo(ServerInfo serverInfo) {
        ServerInfo infoFromDatabase = getServerInfo(serverInfo.getMulticraftId());
        serverInfo.setHiddenPlayers(infoFromDatabase.getHiddenPlayers());
        serverInfo.setMaxPlayers(infoFromDatabase.getMaxPlayers());
        serverInfo.setPlayers(infoFromDatabase.getPlayers());
        serverInfo.setState(infoFromDatabase.getState());
        serverInfo.setStatus(infoFromDatabase.getStatus());
        serverInfo.setType(infoFromDatabase.getType());
    }
    
    public List<ServerInfo> getAllServers() {
        List<ServerInfo> servers = new ArrayList<>();
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select multicraftId from serverinfo;");
            while (resultSet.next()) {
                int multicraftId = resultSet.getInt("multicraftId");
                ServerInfo serverInfo = getServerInfo(multicraftId);
                if (serverInfo != null) {
                    servers.add(serverInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }
    
    public void getAllServersAsync(Consumer<List<ServerInfo>> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            List<ServerInfo> allServers = getAllServers();
            if (allServers != null && !allServers.isEmpty()) {
                action.accept(allServers);
            }
        });
    }
    
    public ServerInfo getServerInfo(int multicraftId) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from serverinfo where multicraftId='" + multicraftId + "';");
            if (resultSet.next()) {
                String ip = resultSet.getString("ip");
                String name = resultSet.getString("name");
                int port = resultSet.getInt("port");
                int players = resultSet.getInt("players");
                int maxPlayers = resultSet.getInt("maxPlayers");
                int hiddenPlayers = resultSet.getInt("hiddenPlayers");
                String type = resultSet.getString("type");
                String status = resultSet.getString("status");
                String state = resultSet.getString("state");
                return new ServerInfo(multicraftId, ip, name, port, players, maxPlayers, hiddenPlayers, type, status, state);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void getServerInfoAsync(int multicraftId, Consumer<ServerInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            ServerInfo serverInfo = getServerInfo(multicraftId);
            if (serverInfo != null) {
                action.accept(serverInfo);
            }
        });
    }
    
    public void pushServerInfo(ServerInfo serverInfo) {
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select * from serverinfo where multicraftId='" + serverInfo.getMulticraftId() + "';");
                PreparedStatement preparedStatement;
                if (resultSet.next()) {
                    preparedStatement = connection.prepareStatement("update serverinfo set ip=?, name=?, port=?, players=?, maxPlayers=?, hiddenPlayers=?, type=?, status=?, state=? where multicraftId='" + serverInfo.getMulticraftId() + "';");
                } else {
                    preparedStatement = connection.prepareStatement("insert into serverinfo(multicraftId, ip, name, port, players, maxPlayers, hiddenPlayers, type, status, state) values (" + serverInfo.getMulticraftId() + ", ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                }
                preparedStatement.setString(1, serverInfo.getIp());
                preparedStatement.setString(2, serverInfo.getName());
                preparedStatement.setInt(3, serverInfo.getPort());
                preparedStatement.setInt(4, serverInfo.getPlayers());
                preparedStatement.setInt(5, serverInfo.getMaxPlayers());
                preparedStatement.setInt(6, serverInfo.getHiddenPlayers());
                preparedStatement.setString(7, serverInfo.getType());
                preparedStatement.setString(8, serverInfo.getStatus());
                preparedStatement.setString(9, serverInfo.getState());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void pushServerInfoAsync(ServerInfo serverInfo) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> pushServerInfo(serverInfo));
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
                            try (PreparedStatement preparedStatement = connection.prepareStatement("update stats set value=?, modified=? where id='" + stat.getId() + "'")) {
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
        NexusAPI.getApi().getPlayerManager().updateNexusTeamRank(player);
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            boolean exists = false;
            try (Statement queryStatement = connection.createStatement()) {
                ResultSet existingResultSet = queryStatement.executeQuery("SELECT * FROM players WHERE uuid='" + player.getUniqueId() + "';");
                StringBuilder sb = new StringBuilder();
                for (Entry<Rank, Long> entry : player.getRanks().entrySet()) {
                    sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(",");
                }
                
                String ranks;
                if (sb.length() > 0) {
                    ranks = sb.substring(0, sb.toString().length() - 1);
                } else {
                    ranks = "";
                }
                
                String unlockedTags = convertTags(player);
                
                String sql;
                if (!existingResultSet.next()) {
                    sql = "INSERT INTO players(version, uuid, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, ranks, tag, unlockedTags, lastPlaytimeNotification) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                } else {
                    sql = "UPDATE players SET version=?, uuid=?, firstJoined=?, lastLogin=?, lastLogout=?, playtime=?, lastKnownName=?, ranks=?, tag=?, unlockedTags=?, lastPlaytimeNotification=? WHERE uuid='" + player.getUniqueId() + "';";
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
    
                    insertStatement.setString(10, unlockedTags);
                    insertStatement.setInt(11, player.getLastPlaytimeNotification());
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
    
    public String convertTags(NexusPlayer player) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : player.getUnlockedTags()) {
            sb.append(tag.getName()).append(",");
        }
        String unlockedTags;
        if (sb.length() > 1) {
            unlockedTags = sb.substring(0, sb.toString().length() - 1);
        } else {
            unlockedTags = "";
        }
        return unlockedTags;
    }
    
    public Set<Tag> parseTags(String rawUnlockedTags) {
        Set<Tag> unlockedTags = new HashSet<>();
        if (rawUnlockedTags != null && !rawUnlockedTags.equals("")) {
            String[] split = rawUnlockedTags.split(",");
            for (String s : split) {
                Tag t = NexusAPI.getApi().getTagManager().getTag(s);
                if (t != null) {
                    unlockedTags.add(t);
                }
            }
        }
        return unlockedTags;
    }
    
    public void refreshPlayerStats(NexusPlayer nexusPlayer) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet statsResultSet = statement.executeQuery("select * from stats where uuid='" + nexusPlayer.getUniqueId() + "';");
            while (statsResultSet.next()) {
                int id = statsResultSet.getInt("id");
                String name = statsResultSet.getString("name");
                String rawValue = statsResultSet.getString("value");
                long created = Long.parseLong(statsResultSet.getString("created"));
                long modified = Long.parseLong(statsResultSet.getString("modified"));
                UUID uuid = UUID.fromString(statsResultSet.getString("uuid"));
                
                if (!StatRegistry.isValidStat(name)) {
                    continue;
                }
                
                Stat<? extends Number> stat;
                if (StatRegistry.isIntegerStat(name)) {
                    stat = StatRegistry.instantiateIntegerStat(id, name, nexusPlayer.getUniqueId(), Integer.parseInt(rawValue), created, modified);
                } else if (StatRegistry.isDoubleStat(name)) {
                    stat = StatRegistry.instantiateDoubleStat(id, name, nexusPlayer.getUniqueId(), Double.parseDouble(rawValue), created, modified);
                } else {
                    continue;
                }
                
                nexusPlayer.addStat((Stat<Number>) stat);
            }
            
            ResultSet statChangesResultSet = statement.executeQuery("select * from statchanges where uuid='" + nexusPlayer.getUniqueId() + "'");
            while (statChangesResultSet.next()) {
                int id = statChangesResultSet.getInt("id");
                String name = statChangesResultSet.getString("statName");
                String rawValue = statChangesResultSet.getString("value");
                Operator operator = Operator.valueOf(statChangesResultSet.getString("operator"));
                long timestamp = Long.parseLong(statChangesResultSet.getString("timestamp"));
                UUID uuid = UUID.fromString(statChangesResultSet.getString("uuid"));
                Number value;
                if (StatRegistry.isIntegerStat(name)) {
                    value = Integer.parseInt(rawValue);
                } else if (StatRegistry.isDoubleStat(name)) {
                    value = Double.parseDouble(rawValue);
                } else {
                    continue;
                }
                
                StatChange<Number> statChange = new StatChange<>(id, nexusPlayer.getUniqueId(), name, value, operator, timestamp);
                nexusPlayer.addStatChange(statChange);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public NexusPlayer loadPlayer(UUID uuid) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet playerResultSet = statement.executeQuery("SELECT * FROM players WHERE uuid='" + uuid.toString() + "';");
            if (playerResultSet.next()) {
                int version = Integer.parseInt(playerResultSet.getString("version"));
                long firstJoined = 0, lastLogin = 0, lastLogout = 0, playtime = 0;
                String lastKnownName = "", rawRanks;
                Map<Rank, Long> ranks = null;
                Tag tag = null;
                Set<Tag> unlockedTags = new HashSet<>();
                if (version >= 2) {
                    firstJoined = Long.parseLong(playerResultSet.getString("firstJoined"));
                    lastLogin = Long.parseLong(playerResultSet.getString("lastLogin"));
                    playtime = Long.parseLong(playerResultSet.getString("playtime"));
                    lastKnownName = playerResultSet.getString("lastKnownName");
                    rawRanks = playerResultSet.getString("ranks");
                    ranks = parseRanks(rawRanks);
                }
                
                if (version >= 3) {
                    tag = NexusAPI.getApi().getTagManager().getTag(playerResultSet.getString("tag"));
                    lastLogout = Long.parseLong(playerResultSet.getString("lastLogout"));
                }
                
                if (version >= 4) {
                    unlockedTags = parseTags(playerResultSet.getString("unlockedTags"));
                }
                
                int lastPlaytimeNotification = playerResultSet.getInt("lastPlaytimeNotification");
                
                NexusPlayer nexusPlayer = NexusAPI.getApi().getPlayerFactory().createPlayer(uuid, ranks, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, tag, unlockedTags, lastPlaytimeNotification);
                refreshPlayerStats(nexusPlayer);
                return nexusPlayer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Map<Rank, Long> parseRanks(String rawRanks) {
        Map<Rank, Long> ranks = new TreeMap<>();
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
        return ranks;
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
