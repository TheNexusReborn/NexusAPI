package com.thenexusreborn.api;

import com.thenexusreborn.api.data.*;
import com.thenexusreborn.api.data.objects.*;
import com.thenexusreborn.api.gamearchive.*;
import com.thenexusreborn.api.migration.Migrator;
import com.thenexusreborn.api.network.*;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.player.Preference.Info;
import com.thenexusreborn.api.punishment.*;
import com.thenexusreborn.api.registry.*;
import com.thenexusreborn.api.server.*;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.thread.ThreadFactory;
import com.thenexusreborn.api.tournament.Tournament;
import com.thenexusreborn.api.util.*;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public abstract class NexusAPI {
    private static NexusAPI instance;
    public static final Phase PHASE = Phase.ALPHA;
    
    public static void setApi(NexusAPI api) {
        instance = api;
    }
    
    public static NexusAPI getApi() {
        return instance;
    }
    
    protected final Logger logger;
    protected final DataManager dataManager;
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
    
    protected final Migrator migrator;
    
    public NexusAPI(Environment environment, NetworkContext context, Logger logger, PlayerManager playerManager, ThreadFactory threadFactory, PlayerFactory playerFactory, ServerManager serverManager) {
        this.environment = environment;
        this.logger = logger;
        this.networkManager = new NetworkManager(context);
        this.dataManager = new DataManager();
        this.playerManager = playerManager;
        this.threadFactory = threadFactory;
        this.playerFactory = playerFactory;
        this.serverManager = serverManager;
        this.punishmentManager = new PunishmentManager();
        this.ioManager = new IOManager(new DatabaseRegistry());
        
        this.migrator = new DataBackendMigrator();
        
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
        getLogger().info("Detected NexusAPI Version: " + version);
        
        try {
            Driver mysqlDriver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(mysqlDriver);
        } catch (SQLException e) {
            getLogger().severe("Error while loading the MySQL driver, disabling plugin");
            e.printStackTrace();
            return;
        }
        
        
        NetworkCommandRegistry networkCommandRegistry = new NetworkCommandRegistry();
        registerNetworkCommands(networkCommandRegistry);
        networkManager.init("localhost", 3408);
        
        DatabaseRegistry databaseRegistry = ioManager.getRegistry();
        registerDatabases(databaseRegistry);
        
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
                this.primaryDatabase = database;
            }
        }
        
        this.ioManager.setup();
        
        File lastMigrationFile = new File(getFolder(), "lastMigration.txt");
        Version previousVersion = null;
        if (lastMigrationFile.exists()) {
            try (FileInputStream fis = new FileInputStream(lastMigrationFile); BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                previousVersion = new Version(reader.readLine());
            }
        }
        
        boolean migrationSuccess = false;
        
        if (migrator != null) {
            int compareResult = this.version.compareTo(previousVersion);
            if (compareResult > 0) {
                if (migrator.getTargetVersion().equals(this.version)) {
                    migrationSuccess = migrator.migrate();
                    
                    if (!migrationSuccess) {
                        NexusAPI.logMessage(Level.INFO, "Error while processing migration", "Migrator Class: " + migrator.getClass().getName());
                    }
                }
            }
        }
        
        if (migrator == null || migrationSuccess) {
            if (!lastMigrationFile.exists()) {
                lastMigrationFile.createNewFile();
            }
            String version = getVersion().getMajor() + "." + getVersion().getMinor();
            if (getVersion().getPatch() > 0) {
                version += "." + getVersion().getPatch();
            }
            version += "-" + getVersion().getStage().name();
            try (FileOutputStream fos = new FileOutputStream(lastMigrationFile); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
                writer.write(version);
                writer.flush();
            }
        }
        
        statRegistry = StatHelper.getRegistry();
        statRegistry.register("xp", StatType.DOUBLE, 0.0);
        statRegistry.register("level", StatType.INTEGER, 0);
        statRegistry.register("nexites", StatType.DOUBLE, 0.0);
        statRegistry.register("credits", StatType.DOUBLE, 0.0);
        statRegistry.register("playtime", StatType.LONG, 0L);
        statRegistry.register("firstjoined", StatType.LONG, 0L);
        statRegistry.register("lastlogin", StatType.LONG, 0L);
        statRegistry.register("lastlogout", StatType.LONG, 0L);
        statRegistry.register("prealpha", StatType.BOOLEAN, false);
        statRegistry.register("alpha", StatType.BOOLEAN, false);
        statRegistry.register("beta", StatType.BOOLEAN, false);
        statRegistry.register("tag", StatType.STRING, "");
        statRegistry.register("online", StatType.BOOLEAN, false);
        statRegistry.register("server", StatType.STRING, "");
        statRegistry.register("unlockedtags", StatType.STRING_SET, new HashSet<>());
        List<Stat.Info> statInfos = primaryDatabase.get(Stat.Info.class);
        for (Stat.Info statInfo : statInfos) {
            StatHelper.getRegistry().register(statInfo);
        }
        registerStats(statRegistry);
        
        preferenceRegistry = new PreferenceRegistry();
        preferenceRegistry.register("vanish", "Vanish", "A staff only thing where you can be completely invisible", false);
        preferenceRegistry.register("incognito", "Incognito", "A media+ thing where you can be hidden from others", false);
        List<Preference.Info> preferenceInfos = primaryDatabase.get(Preference.Info.class);
        for (Info preferenceInfo : preferenceInfos) {
            preferenceRegistry.register(preferenceInfo);
        }
        
        registerPreferences(preferenceRegistry);
        
        for (Stat.Info statInfo : StatHelper.getRegistry().getObjects()) {
            getPrimaryDatabase().push(statInfo);
        }
        
        serverManager.setupCurrentServer();
        
        for (Punishment punishment : getPrimaryDatabase().get(Punishment.class)) {
            punishmentManager.addPunishment(punishment);
        }
        
        List<Tournament> tournaments = getPrimaryDatabase().get(Tournament.class);
        for (Tournament t : tournaments) {
            if (t.isActive()) {
                this.tournament = t;
            }
        }
        
        playerManager.getIpHistory().addAll(getPrimaryDatabase().get(IPEntry.class));
        
        Database database = getPrimaryDatabase();
        List<Row> playerRows = database.executeQuery("select * from players;");
        for (Row row : playerRows) {
            CachedPlayer cachedPlayer = new CachedPlayer(row.getLong("id"), UUID.fromString(row.getString("uniqueId")), row.getString("name"));
            playerManager.getCachedPlayers().put(cachedPlayer.getUniqueId(), cachedPlayer);
        }
        
        List<Row> statsRows = database.executeQuery("select `name`,`uuid`,`value` from stats where `name`='server' or `name`='online' or `name`='lastlogout';");
        for (Row row : statsRows) {
            String name = row.getString("name");
            UUID uuid = UUID.fromString(row.getString("uuid"));
            String rawValue = row.getString("value");
            Stat.Info info = StatHelper.getInfo(name);
            Object value = StatHelper.parseValue(info.getType(), rawValue);
            CachedPlayer player = playerManager.getCachedPlayers().get(uuid);
            if (name.equalsIgnoreCase("server")) {
                player.setServer((String) value);
            } else if (name.equalsIgnoreCase("online")) {
                player.setOnline((boolean) value);
            } else if (name.equalsIgnoreCase("lastlogout")) {
                player.setLastLogout((long) value);
            }
        }
        
        List<Row> preferencesRows = database.executeQuery("select `name`, `uuid`, `value` from preferences where `name`='vanish' or `name`='incognito'");
        for (Row row : preferencesRows) {
            String name = row.getString("name");
            UUID uuid = UUID.fromString("uuid");
            boolean value = row.getBoolean("value");
            CachedPlayer player = playerManager.getCachedPlayers().get(uuid);
            if (name.equalsIgnoreCase("vanish")) {
                player.setVanish(value);
            } else if (name.equalsIgnoreCase("incognito")) {
                player.setIncognito(value);
            }
        }
        
        for (IPEntry entry : playerManager.getIpHistory()) {
            CachedPlayer player = playerManager.getCachedPlayers().get(entry.getUuid());
            player.getIpHistory().add(entry);
        }
        
        //TODO Have the ranks and tags things updated via the network framework instead of having to query the database
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
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
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
