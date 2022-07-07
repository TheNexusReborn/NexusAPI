package com.thenexusreborn.api;

import com.thenexusreborn.api.data.*;
import com.thenexusreborn.api.network.*;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.*;
import com.thenexusreborn.api.registry.*;
import com.thenexusreborn.api.server.ServerManager;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;
import com.thenexusreborn.api.thread.ThreadFactory;
import com.thenexusreborn.api.tournament.Tournament;

import java.sql.*;
import java.util.List;
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
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public final void init() throws Exception {
        try {
            Driver mysqlDriver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(mysqlDriver);
        } catch (SQLException e) {
            getLogger().severe("Error while loading the MySQL driver, disabling plugin");
            e.printStackTrace();
            return;
        }
        
        DatabaseRegistry databaseRegistry = new DatabaseRegistry();
        registerDatabases(databaseRegistry);
        
        this.ioManager = new IOManager(databaseRegistry);
        this.ioManager.setup();
    
        StatRegistry registry = StatHelper.getRegistry();
        registry.register("xp", StatType.DOUBLE, 0.0);
        //TODO registry.register("level", StatType.INTEGER, 0);
        registry.register("nexites", StatType.DOUBLE, 0.0);
        registry.register("credits", StatType.DOUBLE, 0.0);
        registry.register("playtime", StatType.LONG, 0);
        registry.register("firstjoined", StatType.LONG, 0);
        registry.register("lastlogin", StatType.LONG, 0);
        registry.register("lastlogout", StatType.LONG, 0);
        registry.register("prealpha", StatType.BOOLEAN, false);
        registry.register("alpha", StatType.BOOLEAN, false);
        registry.register("beta", StatType.BOOLEAN, false);
        registry.register("tag", StatType.STRING, "");
        registerStats(registry);
        
        NetworkCommandRegistry networkCommandRegistry = new NetworkCommandRegistry();
        registerNetworkCommands(networkCommandRegistry);
        networkManager.init("localhost", 3408);

        dataManager.setupMysql();
    
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("insert into statinfo (name, type, defaultValue) values (?, ?, ?);")) {
            for (Info info : registry.getObjects()) {
                if (!dataManager.getStatsInDatabase().contains(info.getName())) {
                    statement.setString(1, info.getName());
                    statement.setString(2, info.getType().name());
                    statement.setString(3, StatHelper.serializeStatValue(info.getType(), info.getDefaultValue()));
                    statement.executeUpdate();
                }
            }
        }
        
        serverManager.setupCurrentServer();
        
        List<Punishment> punishments = dataManager.getPunishments();
        for (Punishment punishment : punishments) {
            punishmentManager.addPunishment(punishment);
        }
        
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select id from tournaments;");
            if (resultSet.next()) {
                this.tournament = NexusAPI.getApi().getDataManager().getTournament(resultSet.getInt("id"));
            }
        }
        
        playerManager.getIpHistory().putAll(getDataManager().getIpHistory());
    }
    
    public abstract void registerDatabases(DatabaseRegistry registry);
    
    public abstract void registerStats(StatRegistry registry);
    
    public abstract void registerNetworkCommands(NetworkCommandRegistry registry);
    
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
    
    public abstract Connection getConnection() throws SQLException;
    
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
}
