package com.thenexusreborn.api.data;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.gamearchive.*;
import com.thenexusreborn.api.helper.MojangHelper;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.*;
import com.thenexusreborn.api.server.ServerInfo;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.util.Operator;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("DuplicatedCode")
public class DataManager {
    public void setupMysql() throws SQLException {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players(version varchar(10), uuid varchar(36) NOT NULL, firstJoined varchar(100), lastLogin varchar(100), lastLogout varchar(100), playtime varchar(100), lastKnownName varchar(16), tag varchar(30), ranks varchar(1000), unlockedTags varchar(1000), prealpha varchar(5), alpha varchar(5), beta varchar(5));");
            statement.execute("CREATE TABLE IF NOT EXISTS stats(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), name varchar(100), value varchar(1000), created varchar(100), modified varchar(100));");
            statement.execute("CREATE TABLE IF NOT EXISTS statchanges(id int PRIMARY KEY NOT NULL AUTO_INCREMENT, uuid varchar(36), statName varchar(100), value varchar(100), operator varchar(50), timestamp varchar(100));");
            statement.execute("create table if not exists serverinfo(multicraftId int primary key not null, ip varchar(50), name varchar(100), port int, players int, maxPlayers int, hiddenPlayers int, type varchar(100), status varchar(100), state varchar(100));");
            statement.execute("create table if not exists games(id int primary key not null auto_increment, start long, end long, serverName varchar(100), players varchar(500), winner varchar(20), mapName varchar(50), settings varchar(1000), firstBlood varchar(20), playerCount int, length long);");
            statement.execute("create table if not exists gameactions(gameId int, timestamp long, type varchar(100), value varchar(1000));");
            statement.execute("create table if not exists punishments(id int primary key not null auto_increment, date varchar(100), length varchar(100), actor varchar(100), target varchar(100), server varchar(100), reason varchar(200), type varchar(30), visibility varchar(30), pardonInfo varchar(500), acknowledgeInfo varchar(500));");
            statement.execute("create table if not exists iphistory(ip varchar(100), uuid varchar(36))");
            
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
                    statement.execute("alter table players add column prealpha varchar(5) after unlockedTags;");
                    statement.execute("alter table players add column alpha varchar(5) after prealpha;");
                    statement.execute("alter table players add column beta varchar(5) after alpha;");
                }
                
                for (NexusPlayer player : players.values()) {
                    NexusAPI.getApi().getDataManager().pushPlayer(player);
                }
                
                players.clear();
                NexusAPI.getApi().getLogger().info("Conversion complete");
            }
        }
    }
    
    public NexusPlayer loadPlayer(String name) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet nameSet = statement.executeQuery("select uuid from players where lastKnownName='" + name + "';");
            if (nameSet.next()) {
                return loadPlayer(UUID.fromString(nameSet.getString("uuid")));
            }
        
            UUID uuid = MojangHelper.getUUIDFromName(name);
            if (uuid == null) {
                return null;
            }
        
            ResultSet uuidSet = statement.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
            if (uuidSet.next()) {
                return loadPlayer(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public GameInfo getGameInfo(int id) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from games where id='" + id + "';");
            if (resultSet.next()) {
                long gameStart = resultSet.getLong("start");
                long gameEnd = resultSet.getLong("end");
                String serverName = resultSet.getString("serverName");
                String[] players = resultSet.getString("players").split(",");
                String winner = resultSet.getString("winner");
                String mapName = resultSet.getString("mapName");
                String settings = resultSet.getString("settings");
                String firstBlood = resultSet.getString("firstBlood");
                int playerCount = resultSet.getInt("playerCount");
                long length = resultSet.getLong("length");
                GameInfo gameInfo = new GameInfo(id, gameStart, gameEnd, serverName, players, winner, mapName, settings, firstBlood, playerCount, length);
                ResultSet actionSet = statement.executeQuery("select * from gameactions where gameId='" + id + "';");
                while (actionSet.next()) {
                    long timestamp = actionSet.getLong("timestamp");
                    String type = actionSet.getString("type");
                    String value = actionSet.getString("value");
                    GameAction gameAction = new GameAction(id, timestamp, type, value);
                    gameInfo.getActions().add(gameAction);
                }
                return gameInfo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void pushGameInfo(GameInfo gameInfo) {
        try (Connection connection = NexusAPI.getApi().getConnection(); PreparedStatement gameStatement = connection.prepareStatement("insert into games(start, end, serverName, players, winner, mapName, settings, firstBlood, playerCount, length) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            gameStatement.setLong(1, gameInfo.getGameStart());
            gameStatement.setLong(2, gameInfo.getGameEnd());
            gameStatement.setString(3, gameInfo.getServerName());
            StringBuilder sb = new StringBuilder();
            for (String player : gameInfo.getPlayers()) {
                sb.append(player).append(",");
            }
            gameStatement.setString(4, sb.substring(0, sb.length() - 1));
            gameStatement.setString(5, gameInfo.getWinner());
            gameStatement.setString(6, gameInfo.getMapName());
            gameStatement.setString(7, gameInfo.getSettings());
            gameStatement.setString(8, gameInfo.getFirstBlood());
            gameStatement.setInt(9, gameInfo.getPlayerCount());
            gameStatement.setLong(10, gameInfo.getLength());
            gameStatement.executeUpdate();
            ResultSet generatedKeys = gameStatement.getGeneratedKeys();
            generatedKeys.next();
            gameInfo.setId(generatedKeys.getInt(1));
            
            PreparedStatement actionStatement = connection.prepareStatement("insert into gameactions(gameId, timestamp, type, value) values (?, ?, ?, ?);");
            for (GameAction action : gameInfo.getActions()) {
                actionStatement.setInt(1, gameInfo.getId());
                actionStatement.setLong(2, action.getTimestamp());
                actionStatement.setString(3, action.getType());
                actionStatement.setString(4, action.getValue());
                actionStatement.addBatch();
            }
            actionStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void pushGameInfoAsync(GameInfo gameInfo, Consumer<GameInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            pushGameInfo(gameInfo);
            if (action != null) {
                action.accept(gameInfo);
            }
        });
    }
    
    public void getGameInfoAsync(int id, Consumer<GameInfo> action) {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            GameInfo gameInfo = getGameInfo(id);
            if (action != null) {
                action.accept(gameInfo);
            }
        });
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
            if (statChange.getId() != 0) {
                try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
                    statement.executeUpdate("delete from statchanges where id='" + statChange.getId() + "'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void pushPlayer(NexusPlayer player) {
        NexusAPI.getApi().getPlayerManager().updateNexusTeamRank(player);
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            boolean exists = false;
            try (Statement queryStatement = connection.createStatement()) {
                ResultSet existingResultSet = queryStatement.executeQuery("SELECT * FROM players WHERE uuid='" + player.getUniqueId() + "';");
                
                String ranks = player.serializeRanks();
                String unlockedTags = convertTags(player);
                
                String sql;
                if (!existingResultSet.next()) {
                    sql = "INSERT INTO players(version, uuid, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, ranks, tag, unlockedTags, prealpha, alpha, beta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                } else {
                    sql = "UPDATE players SET version=?, uuid=?, firstJoined=?, lastLogin=?, lastLogout=?, playtime=?, lastKnownName=?, ranks=?, tag=?, unlockedTags=?, prealpha=?, alpha=?, beta=? WHERE uuid='" + player.getUniqueId() + "';";
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
                    insertStatement.setString(11, player.isPrealpha() + "");
                    insertStatement.setString(12, player.isAlpha() + "");
                    insertStatement.setString(13, player.isBeta() + "");
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
                unlockedTags.add(new Tag(s));
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
                boolean prealpha = false, alpha = false, beta = false;
                if (version >= 2) {
                    firstJoined = Long.parseLong(playerResultSet.getString("firstJoined"));
                    lastLogin = Long.parseLong(playerResultSet.getString("lastLogin"));
                    playtime = Long.parseLong(playerResultSet.getString("playtime"));
                    lastKnownName = playerResultSet.getString("lastKnownName");
                    rawRanks = playerResultSet.getString("ranks");
                    ranks = parseRanks(rawRanks);
                }
                
                if (version >= 3) {
                    String rawTag = playerResultSet.getString("tag");
                    if (rawTag != null && !rawTag.equalsIgnoreCase("null")) {
                        tag = new Tag(rawTag);
                    }
                    lastLogout = Long.parseLong(playerResultSet.getString("lastLogout"));
                }
                
                if (version >= 4) {
                    unlockedTags = parseTags(playerResultSet.getString("unlockedTags"));
                }
                
                if (version >= 5) {
                    prealpha = Boolean.parseBoolean(playerResultSet.getString("prealpha"));
                    alpha = Boolean.parseBoolean(playerResultSet.getString("alpha"));
                    beta = Boolean.parseBoolean(playerResultSet.getString("beta"));
                }
                
                NexusPlayer nexusPlayer = NexusAPI.getApi().getPlayerFactory().createPlayer(uuid, ranks, firstJoined, lastLogin, lastLogout, playtime, lastKnownName, tag, unlockedTags, prealpha, alpha, beta);
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
    
    public void pushPunishment(Punishment punishment) {
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            String sql;
            int returnGeneratedKeys = Statement.NO_GENERATED_KEYS;
            if (punishment.getId() > 0) {
                sql = "update punishments set date=?, length=?, actor=?, target=?, server=?, reason=?, type=?, visibility=?, pardonInfo=?, acknowledgeInfo=? where id='" + punishment.getId() + "';";
            } else {
                sql = "insert into punishments (date, length, actor, target, server, reason, type, visibility, pardonInfo, acknowledgeInfo) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                returnGeneratedKeys = Statement.RETURN_GENERATED_KEYS;
            }
            
            try (PreparedStatement ps = connection.prepareStatement(sql, returnGeneratedKeys)) {
                ps.setString(1, punishment.getDate() + "");
                ps.setString(2, punishment.getLength() + "");
                ps.setString(3, punishment.getActor());
                ps.setString(4, punishment.getTarget());
                ps.setString(5, punishment.getServer());
                ps.setString(6, punishment.getReason());
                ps.setString(7, punishment.getType().name());
                ps.setString(8, punishment.getVisibility().name());
                String piv = "";
                PardonInfo pardonInfo = punishment.getPardonInfo();
                if (pardonInfo != null) {
                    piv = "date=" + pardonInfo.getDate() + ",actor=" + pardonInfo.getActor() + ",reason=" + pardonInfo.getReason();
                }
                ps.setString(9, piv);
                String aiv = "";
                AcknowledgeInfo acknowledgeInfo = punishment.getAcknowledgeInfo();
                if (acknowledgeInfo != null) {
                    aiv = "code=" + acknowledgeInfo.getCode() + ",time=" + acknowledgeInfo.getTime();
                }
                ps.setString(10, aiv);
                ps.executeUpdate();
                if (returnGeneratedKeys == Statement.RETURN_GENERATED_KEYS) {
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    generatedKeys.next();
                    int key = generatedKeys.getInt(1);
                    punishment.setId(key);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Punishment getPunishment(int id) {
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from punishments where id='" + id + "';");
            if (resultSet.next()) {
                long date = Long.parseLong(resultSet.getString("date"));
                long length = Long.parseLong(resultSet.getString("length"));
                String actor = resultSet.getString("actor");
                String target = resultSet.getString("target");
                String server = resultSet.getString("server");
                String reason = resultSet.getString("reason");
                PunishmentType type = PunishmentType.valueOf(resultSet.getString("type"));
                Visibility visibility = Visibility.valueOf(resultSet.getString("visibility"));
                String rawPardonInfo = resultSet.getString("pardonInfo");
                PardonInfo pardonInfo = null;
                if (rawPardonInfo != null && !rawPardonInfo.equals("")) {
                    String[] piSplit = rawPardonInfo.split(",");
                    long pardonDate = 0;
                    String pardonActor = "";
                    String pardonReason = "";
                    if (piSplit != null && piSplit.length == 3) {
                        for (String d : piSplit) {
                            String[] dSplit = d.split("=");
                            if (dSplit != null && dSplit.length == 2) {
                                if (dSplit[0].equalsIgnoreCase("date")) {
                                    pardonDate = Long.parseLong(dSplit[1]);
                                } else if (dSplit[0].equalsIgnoreCase("actor")) {
                                    pardonActor = dSplit[1];
                                } else if (dSplit[0].equalsIgnoreCase("reason")) {
                                    pardonReason = dSplit[1];
                                }
                            }
                        }
                    }
                    pardonInfo = new PardonInfo(pardonDate, pardonActor, pardonReason);
                }
                
                String rawAcknowledgeInfo = resultSet.getString("acknowledgeInfo");
                AcknowledgeInfo acknowledgeInfo = null;
                if (rawAcknowledgeInfo != null && !rawAcknowledgeInfo.equals("")) {
                    String[] piSplit = rawAcknowledgeInfo.split(",");
                    long ackTime = 0;
                    String ackCode = "";
                    if (piSplit != null && piSplit.length == 2) {
                        for (String d : piSplit) {
                            String[] dSplit = d.split("=");
                            if (dSplit != null && dSplit.length == 2) {
                                if (dSplit[0].equalsIgnoreCase("time")) {
                                    ackTime = Long.parseLong(dSplit[1]);
                                } else if (dSplit[0].equalsIgnoreCase("code")) {
                                    ackCode = dSplit[1];
                                }
                            }
                        }
                    }
                    acknowledgeInfo = new AcknowledgeInfo(ackCode, ackTime);
                }
                
                Punishment punishment = new Punishment(date, length, actor, target, server, reason, type, visibility);
                punishment.setId(id);
                punishment.setPardonInfo(pardonInfo);
                punishment.setAcknowledgeInfo(acknowledgeInfo);
    
                String actorCache = "";
                try {
                    UUID uuid = UUID.fromString(punishment.getActor());
                    NexusPlayer actorCachePlayer = NexusAPI.getApi().getPlayerManager().getNexusPlayer(uuid);
                    if (actorCachePlayer == null) {
                        try (Statement s = connection.createStatement()) {
                            ResultSet rs = s.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
                            if (rs.next()) {
                                actorCache = rs.getString("lastKnownName");
                            }
                        }
                    } else {
                        actorCache = actorCachePlayer.getName();
                    }
                } catch (Exception e) {
                    actorCache = actor;
                }
                punishment.setActorNameCache(actorCache);
                
                try {
                    String targetCache = "";
                    UUID uuid = UUID.fromString(punishment.getTarget());
                    NexusPlayer targetCachePlayer = NexusAPI.getApi().getPlayerManager().getNexusPlayer(uuid);
                    if (targetCachePlayer == null) {
                        try (Statement s = connection.createStatement()) {
                            ResultSet rs = s.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
                            if (rs.next()) {
                                targetCache = rs.getString("lastKnownName");
                            }
                        }
                    } else {
                        targetCache = targetCachePlayer.getName();
                    }
                    punishment.setTargetNameCache(targetCache);
                } catch (Exception e) {
                    e.printStackTrace();
                }
    
                try {
                    if (punishment.getPardonInfo() != null) {
                        String removalActorName = "";
                        try {
                            UUID uuid = UUID.fromString(punishment.getPardonInfo().getActor());
                            NexusPlayer actorCachePlayer = NexusAPI.getApi().getPlayerManager().getNexusPlayer(uuid);
                            if (actorCachePlayer == null) {
                                try (Statement s = connection.createStatement()) {
                                    ResultSet rs = s.executeQuery("select lastKnownName from players where uuid='" + uuid + "';");
                                    if (rs.next()) {
                                        removalActorName = rs.getString("lastKnownName");
                                    }
                                }
                            } else {
                                removalActorName = actorCachePlayer.getName();
                            }
                        } catch (Exception e) {
                            removalActorName = punishment.getPardonInfo().getActor();
                        }
                        punishment.getPardonInfo().setActorNameCache(removalActorName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return punishment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Set<UUID> getPlayersByIp(String ip) {
        Set<UUID> players = new HashSet<>();
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from iphistory where ip='" + ip + "';");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                players.add(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return players;
    }
    
    public void addIpHistory(UUID uuid, String ip) {
        try (Connection connection = NexusAPI.getApi().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select * from iphistory where uuid='" + uuid.toString() + "';");
                while (resultSet.next()) {
                    String existingIp = resultSet.getString("ip");
                    if (existingIp.equals(ip)) {
                        return;
                    }
                }
            }
            
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("insert into iphistory(ip, uuid) values ('" + ip + "', '" + uuid + "');");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Set<UUID>> getIpHistory() {
        Map<String, Set<UUID>> ipHistory = new HashMap<>();
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from iphistory");
            while (resultSet.next()) {
                String ip = resultSet.getString("ip");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                if (ipHistory.containsKey(ip)) {
                    ipHistory.get(ip).add(uuid);
                } else {
                    ipHistory.put(ip, new HashSet<>(Collections.singleton(uuid)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ipHistory;
    }
    
    public List<Punishment> getPunishments() {
        List<Punishment> punishments = new ArrayList<>();
        
        try (Connection connection = NexusAPI.getApi().getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select id from punishments;");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                punishments.add(getPunishment(id));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return punishments;
    }
}
