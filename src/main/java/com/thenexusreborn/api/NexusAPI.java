package com.thenexusreborn.api;

import com.thenexusreborn.api.data.*;
import com.thenexusreborn.api.data.objects.Database;
import com.thenexusreborn.api.network.*;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.PunishmentManager;
import com.thenexusreborn.api.registry.*;
import com.thenexusreborn.api.server.ServerManager;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.thread.ThreadFactory;
import com.thenexusreborn.api.tournament.Tournament;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.HashSet;
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
    
    protected Logger logger;
    protected DataManager dataManager;
    protected IOManager ioManager;
    protected PlayerManager playerManager;
    protected ThreadFactory threadFactory;
    protected PlayerFactory playerFactory;
    protected ServerManager serverManager;
    protected final Environment environment;
    protected NetworkManager networkManager;
    protected PunishmentManager punishmentManager;
    protected Tournament tournament;
    protected String version;
    
    protected StatRegistry statRegistry;
    protected PreferenceRegistry preferenceRegistry;
    
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
    
        URL url = NexusAPI.class.getClassLoader().getResource("nexusapi-version.txt");
        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            if (line == null || line.equals("")) {
                logger.warning("Could not find the NexusAPI Version.");
            } else {
                this.version = line;
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
    
        StatRegistry statRegistry = StatHelper.getRegistry();
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
        registerStats(statRegistry);
        
        PreferenceRegistry preferenceRegistry = new PreferenceRegistry();
        preferenceRegistry.register("vanish", "Vanish", "A staff only thing where you can be completely invisible", false);
        preferenceRegistry.register("incognito", "Incognito", "A media+ thing where you can be hidden from others", false);
        registerPreferences(preferenceRegistry);
    
        NetworkCommandRegistry networkCommandRegistry = new NetworkCommandRegistry();
        registerNetworkCommands(networkCommandRegistry);
        networkManager.init("localhost", 3408);
    
        DatabaseRegistry databaseRegistry = new DatabaseRegistry();
        registerDatabases(databaseRegistry);
        
        this.ioManager = new IOManager(databaseRegistry);
        this.ioManager.setup();
        
        //TODO
//        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("insert into statinfo (name, type, defaultValue) values (?, ?, ?);")) {
//            for (Info info : registry.getObjects()) {
//                if (!dataManager.getStatsInDatabase().contains(info.getName())) {
//                    statement.setString(1, info.getName());
//                    statement.setString(2, info.getType().name());
//                    statement.setString(3, StatHelper.serializeStatValue(info.getType(), info.getDefaultValue()));
//                    statement.executeUpdate();
//                }
//            }
//        }
        
        //TODO
        //serverManager.setupCurrentServer();
        
        //TODO
//        List<Punishment> punishments = dataManager.getPunishments();
//        for (Punishment punishment : punishments) {
//            punishmentManager.addPunishment(punishment);
//        }
        
        //TODO
//        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
//            ResultSet resultSet = statement.executeQuery("select id from tournaments;");
//            if (resultSet.next()) {
//                this.tournament = NexusAPI.getApi().getDataManager().getTournament(resultSet.getInt("id"));
//            }
//        }
        
        //TODO
        //playerManager.getIpHistory().addAll(getDataManager().getIpHistory());
    }
    
    public abstract void registerDatabases(DatabaseRegistry registry);
    
    public abstract void registerStats(StatRegistry registry);
    
    public abstract void registerNetworkCommands(NetworkCommandRegistry registry);
    
    public abstract void registerPreferences(PreferenceRegistry registry);
    
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
    
    public String getVersion() {
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
        return null; //TODO
    }
    
    public StatRegistry getStatRegistry() {
        return statRegistry;
    }
    
    public PreferenceRegistry getPreferenceRegistry() {
        return preferenceRegistry;
    }
}
