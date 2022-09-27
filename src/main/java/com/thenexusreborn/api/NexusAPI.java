package com.thenexusreborn.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thenexusreborn.api.data.IOManager;
import com.thenexusreborn.api.data.codec.RanksCodec;
import com.thenexusreborn.api.data.objects.Database;
import com.thenexusreborn.api.data.objects.Row;
import com.thenexusreborn.api.gamearchive.GameAction;
import com.thenexusreborn.api.gamearchive.GameInfo;
import com.thenexusreborn.api.network.NetworkContext;
import com.thenexusreborn.api.network.NetworkManager;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.nickname.Nickname;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentManager;
import com.thenexusreborn.api.registry.DatabaseRegistry;
import com.thenexusreborn.api.registry.NetworkCommandRegistry;
import com.thenexusreborn.api.registry.PreferenceRegistry;
import com.thenexusreborn.api.registry.StatRegistry;
import com.thenexusreborn.api.server.*;
import com.thenexusreborn.api.stats.Stat;
import com.thenexusreborn.api.stats.Stat.Info;
import com.thenexusreborn.api.stats.StatChange;
import com.thenexusreborn.api.stats.StatHelper;
import com.thenexusreborn.api.stats.StatType;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.thread.ThreadFactory;
import com.thenexusreborn.api.tournament.Tournament;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NexusAPI {
    private static NexusAPI instance;
    public static final Phase PHASE = Phase.ALPHA;
    
    public static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
    
    public static void setApi(NexusAPI api) {
        instance = api;
    }
    
    public static NexusAPI getApi() {
        return instance;
    }
    
    protected final Logger logger;
    protected final IOManager ioManager;
    protected final PlayerManager playerManager;
    protected final ThreadFactory threadFactory;
    protected final PlayerFactory playerFactory;
    protected final ServerManager serverManager;
    protected final Environment environment;
    protected final NetworkManager networkManager;
    protected final PunishmentManager punishmentManager;
    protected Tournament tournament;
    protected Version version;
    
    protected StatRegistry statRegistry;
    protected PreferenceRegistry preferenceRegistry;
    
    protected Database primaryDatabase;
    
    public NexusAPI(Environment environment, NetworkContext context, Logger logger, PlayerManager playerManager, ThreadFactory threadFactory, PlayerFactory playerFactory, ServerManager serverManager) {
        this.environment = environment;
        this.logger = logger;
        this.networkManager = new NetworkManager(context);
        this.playerManager = playerManager;
        this.threadFactory = threadFactory;
        this.playerFactory = playerFactory;
        this.serverManager = serverManager;
        this.punishmentManager = new PunishmentManager();
        this.ioManager = new IOManager(new DatabaseRegistry());
        
        URL url = NexusAPI.class.getClassLoader().getResource("nexusapi-version.txt");
        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            if (line == null || line.equals("")) {
                logger.warning("Could not find the NexusAPI Version.");
            } else {
                this.version = new Version(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public final void init() throws Exception {
        getLogger().info("Loading NexusAPI Version v" + this.version);
        
        try {
            for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
                Driver driver = e.nextElement();
                DriverManager.deregisterDriver(driver);
            }
            Driver mysqlDriver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(mysqlDriver);
            getLogger().info("Registered the correct MySQL Driver");
        } catch (SQLException e) {
            getLogger().severe("Error while loading the MySQL driver, disabling plugin");
            throw e;
        }
        
        NetworkCommandRegistry networkCommandRegistry = new NetworkCommandRegistry();
        registerNetworkCommands(networkCommandRegistry);
        networkCommandRegistry.register(new NetworkCommand("updaterank", (cmd, origin, args) -> {
            UUID uuid = UUID.fromString(args[0]);
            String action = args[1];
            Rank rank = Rank.parseRank(args[2]);
            long expire = args.length > 3 ? Long.parseLong(args[3]) : -1;
            
            NexusPlayer nexusPlayer = getPlayerManager().getNexusPlayer(uuid);
            CachedPlayer cachedPlayer = getPlayerManager().getCachedPlayer(uuid);
            if (nexusPlayer != null) {
                if (action.equals("add")) {
                    nexusPlayer.addRank(rank, expire);
                } else if (action.equals("remove")) {
                    nexusPlayer.removeRank(rank);
                } else if (action.equals("set")) {
                    nexusPlayer.setRank(rank, expire);
                }
            } else if (cachedPlayer != null) {
                if (action.equals("add")) {
                    cachedPlayer.getRanks().put(rank, expire);
                } else if (action.equals("remove")) {
                    cachedPlayer.getRanks().remove(rank);
                } else if (action.equals("set")) {
                    cachedPlayer.getRanks().clear();
                    cachedPlayer.getRanks().put(rank, expire);
                }
            }
        }));
        
        networkCommandRegistry.register(new NetworkCommand("updatetag", ((cmd, origin, args) -> {
            UUID uuid = UUID.fromString(args[0]);
            String action = args[1];
            String tag = args.length > 2 ? args[2] : "";
            NexusPlayer player = playerManager.getNexusPlayer(uuid);
            if (action.equalsIgnoreCase("reset")) {
                if (player != null) {
                    player.setTag(null);
                }
            } else if (action.equalsIgnoreCase("set")) {
                if (player != null) {
                    player.setTag(new Tag(tag));
                }
            } else {
                if (player == null) {
                    player = playerManager.getCachedPlayer(uuid).loadFully();
                }
                
                if (action.equalsIgnoreCase("unlock")) {
                    player.unlockTag(tag);
                } else if (action.equalsIgnoreCase("remove")) {
                    player.lockTag(tag);
                }
            }
        })));
        
        networkManager.init("localhost", 3408);
        for (NetworkCommand netCmd : networkCommandRegistry.getObjects()) {
            networkManager.addCommand(netCmd);
        }
        getLogger().info("Loaded the Networking System");
        
        DatabaseRegistry databaseRegistry = ioManager.getRegistry();
        registerDatabases(databaseRegistry);
        getLogger().info("Registered the databases");
        
        for (Database database : databaseRegistry.getObjects()) {
            if (database.isPrimary()) {
                database.registerClass(IPEntry.class);
                database.registerClass(Stat.Info.class);
                database.registerClass(Stat.class);
                database.registerClass(StatChange.class);
                database.registerClass(Preference.Info.class);
                database.registerClass(Preference.class);
                database.registerClass(NexusPlayer.class);
                database.registerClass(ServerInfo.class);
                database.registerClass(GameInfo.class);
                database.registerClass(GameAction.class);
                database.registerClass(Punishment.class);
                database.registerClass(Tournament.class);
                database.registerClass(Nickname.class);
                this.primaryDatabase = database;
                getLogger().info("Found the Primary Database: " + this.primaryDatabase.getHost() + "/" + this.primaryDatabase.getName());
            }
        }
        
        if (primaryDatabase == null) {
            throw new SQLException("Could not find the primary database.");
        }
        
        this.ioManager.setup();
        getLogger().info("Successfully setup the database tables");
        
        statRegistry = StatHelper.getRegistry();
        
        List<Stat.Info> statInfos = primaryDatabase.get(Stat.Info.class);
        
        for (Info statInfo : statInfos) {
            statRegistry.register(statInfo);
        }
        
        statRegistry.register("xp", "Experience", StatType.DOUBLE, 0.0);
        statRegistry.register("level", "Level", StatType.INTEGER, 0);
        statRegistry.register("nexites", "Nexites", StatType.DOUBLE, 0.0);
        statRegistry.register("credits", "Credits", StatType.DOUBLE, 0.0);
        statRegistry.register("playtime", "Playtime", StatType.LONG, 0L);
        statRegistry.register("firstjoined", "First Joined", StatType.LONG, 0L);
        statRegistry.register("lastlogin", "Last Login", StatType.LONG, 0L);
        statRegistry.register("lastlogout", "Last Logout", StatType.LONG, 0L);
        statRegistry.register("prealpha", "PreAlpha", StatType.BOOLEAN, false);
        statRegistry.register("alpha", "Alpha", StatType.BOOLEAN, false);
        statRegistry.register("privatealpha", "Private Alpha", StatType.BOOLEAN, false);
        statRegistry.register("beta", "Beta", StatType.BOOLEAN, false);
        statRegistry.register("tag", "Tag", StatType.STRING, "");
        statRegistry.register("online", "Online", StatType.BOOLEAN, false);
        statRegistry.register("server", "Server", StatType.STRING, "");
        statRegistry.register("unlockedtags", "Unlocked Tags", StatType.STRING_SET, new HashSet<>());
        registerStats(statRegistry);
        
        for (Stat.Info statInfo : StatHelper.getRegistry().getObjects()) {
            getPrimaryDatabase().push(statInfo);
        }
        
        getLogger().info("Pushed stat types to the database");
        
        getLogger().info("Registered Stat types");
        
        preferenceRegistry = new PreferenceRegistry();
        preferenceRegistry.register("vanish", "Vanish", "A staff only thing where you can be completely invisible", false);
        preferenceRegistry.register("incognito", "Incognito", "A media+ thing where you can be hidden from others", false);
        preferenceRegistry.register("fly", "Fly", "A donor perk that allows you to fly in hubs and lobbies", false);
        
        registerPreferences(preferenceRegistry);
        
        List<Preference.Info> preferenceInfos = primaryDatabase.get(Preference.Info.class);
        for (Preference.Info preferenceInfo : preferenceInfos) {
            preferenceRegistry.register(preferenceInfo);
        }
        
        getLogger().info("Registered preference types");
        for (Preference.Info object : preferenceRegistry.getObjects()) {
            getPrimaryDatabase().push(object);
        }
        getLogger().info("Pushed preference types to the database");
        
        serverManager.setupCurrentServer();
        getLogger().info("Set up the current server");
        
        for (Punishment punishment : getPrimaryDatabase().get(Punishment.class)) {
            punishmentManager.addPunishment(punishment);
        }
        getLogger().info("Cached punishments in memory");
        
        List<Tournament> tournaments = getPrimaryDatabase().get(Tournament.class);
        for (Tournament t : tournaments) {
            if (t.isActive()) {
                this.tournament = t;
                getLogger().info("Loaded data for tournament: " + tournament.getName());
            }
        }
        
        playerManager.getIpHistory().addAll(getPrimaryDatabase().get(IPEntry.class));
        getLogger().info("Loaded IP History");
        
        Database database = getPrimaryDatabase();
        List<Row> playerRows = database.executeQuery("select * from players;");
        for (Row row : playerRows) {
            CachedPlayer cachedPlayer = new CachedPlayer(row.getLong("id"), UUID.fromString(row.getString("uniqueId")), row.getString("name"));
            cachedPlayer.setRanks(new RanksCodec().decode(row.getString("ranks")));
            playerManager.getCachedPlayers().put(cachedPlayer.getUniqueId(), cachedPlayer);
        }
        getLogger().info("Loaded basic player data (database IDs, Unique IDs and Names) - " + playerRows.size() + " total profiles.");
        
        List<Row> statsRows = database.executeQuery("select `name`,`uuid`,`value` from stats where `name` in ('server','online','lastlogout','unlockedtags','privatealpha');");
        for (Row row : statsRows) {
            String name = row.getString("name");
            UUID uuid = UUID.fromString(row.getString("uuid"));
            String rawValue = row.getString("value");
            Stat.Info info = StatHelper.getInfo(name);
            Object value = StatHelper.parseValue(info.getType(), rawValue);
            CachedPlayer player = playerManager.getCachedPlayer(uuid);
            if (name.equalsIgnoreCase("server")) {
                player.setServer((String) value);
            } else if (name.equalsIgnoreCase("online")) {
                player.setOnline((boolean) value);
            } else if (name.equalsIgnoreCase("lastlogout")) {
                player.setLastLogout((long) value);
            } else if (name.equalsIgnoreCase("unlockedtags")) {
                player.setUnlockedTags((Set<String>) value);
            } else if (name.equalsIgnoreCase("privatealpha")) {
                player.setPrivateAlpha((boolean) value);
            }
        }
        getLogger().info("Loaded stats for player profiles: Current Server, Online Status, and Last Logout time");
        
        List<Row> preferencesRows = database.executeQuery("select `name`, `uuid`, `value` from preferences;");
        for (Row row : preferencesRows) {
            String name = row.getString("name");
            UUID uuid = UUID.fromString(row.getString("uuid"));
            boolean value = row.getBoolean("value");
            CachedPlayer player = playerManager.getCachedPlayers().get(uuid);
            if (name.equalsIgnoreCase("vanish")) {
                player.setVanish(value);
            } else if (name.equalsIgnoreCase("incognito")) {
                player.setIncognito(value);
            } else if (name.equalsIgnoreCase("fly")) {
                player.setFly(value);
            }
        }
        getLogger().info("Loaded preference info for player profiles: Incognito and Vanish");
        
        for (IPEntry entry : playerManager.getIpHistory()) {
            CachedPlayer player = playerManager.getCachedPlayer(entry.getUuid());
            player.getIpHistory().add(entry);
        }
        getLogger().info("Sorted IP History for player profiles.");
        
        getLogger().info("NexusAPI v" + this.version + " load complete.");
    }
    
    public abstract void registerDatabases(DatabaseRegistry registry);
    
    public abstract void registerStats(StatRegistry registry);
    
    public abstract void registerNetworkCommands(NetworkCommandRegistry registry);
    
    public abstract void registerPreferences(PreferenceRegistry registry);
    
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
    
    public Version getVersion() {
        return version;
    }
    
    @Deprecated
    public abstract Connection getConnection() throws SQLException;
    
    public abstract File getFolder();
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public PlayerFactory getPlayerFactory() {
        return playerFactory;
    }
    
    public ServerManager getServerManager() {
        return serverManager;
    }
    
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public Tournament getTournament() {
        return tournament;
    }
    
    public IOManager getIOManager() {
        return ioManager;
    }
    
    public static void logMessage(Level level, String mainMessage, String... debug) {
        Logger logger = NexusAPI.getApi().getLogger();
        logger.log(level, "----------- Nexus Log -----------");
        logger.log(level, mainMessage);
        if (debug != null && debug.length > 0) {
            for (String s : debug) {
                logger.log(level, s);
            }
        }
        logger.log(level, "---------------------------------");
    }
    
    public Database getPrimaryDatabase() {
        return this.primaryDatabase;
    }
    
    public StatRegistry getStatRegistry() {
        return statRegistry;
    }
    
    public PreferenceRegistry getPreferenceRegistry() {
        return preferenceRegistry;
    }
}
