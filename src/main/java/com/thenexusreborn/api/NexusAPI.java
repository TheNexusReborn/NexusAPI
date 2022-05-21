package com.thenexusreborn.api;

import com.thenexusreborn.api.data.DataManager;
import com.thenexusreborn.api.networking.*;
import com.thenexusreborn.api.networking.manager.SocketManager;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.server.ServerManager;
import com.thenexusreborn.api.stats.StatRegistry;
import com.thenexusreborn.api.thread.ThreadFactory;

import java.sql.*;
import java.util.logging.Logger;

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
    protected PlayerManager playerManager;
    protected ThreadFactory threadFactory;
    protected PlayerFactory playerFactory;
    protected ServerManager serverManager;
    protected final Environment environment;
    protected SocketContext socketContext;
    protected SocketManager socketManager;
    
    public NexusAPI(Environment environment, Logger logger, DataManager dataManager, PlayerManager playerManager, ThreadFactory threadFactory, PlayerFactory playerFactory, ServerManager serverManager, SocketContext socketContext) {
        this.environment = environment;
        this.logger = logger;
        this.dataManager = dataManager;
        this.playerManager = playerManager;
        this.threadFactory = threadFactory;
        this.playerFactory = playerFactory;
        this.serverManager = serverManager;
        this.socketContext = socketContext;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public void init() throws Exception {
        try {
            Driver mysqlDriver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(mysqlDriver);
        } catch (SQLException e) {
            getLogger().severe("Error while loading the MySQL driver, disabling plugin");
            e.printStackTrace();
            return;
        }
    
        StatRegistry.registerDoubleStat("nexites", 0); //done
        StatRegistry.registerDoubleStat("credits", 0); //done
        StatRegistry.registerDoubleStat("xp", 0); //done
        StatRegistry.registerIntegerStat("sg_score", 100); //done
        StatRegistry.registerIntegerStat("sg_kills", 0); //done
        StatRegistry.registerIntegerStat("sg_highest_kill_streak", 0); //done
        StatRegistry.registerIntegerStat("sg_games", 0); //done
        StatRegistry.registerIntegerStat("sg_win_streak", 0); //done
        StatRegistry.registerIntegerStat("sg_wins", 0); //done
        StatRegistry.registerIntegerStat("sg_deaths", 0); //done
        StatRegistry.registerIntegerStat("sg_deathmatches_reached", 0); //done
        StatRegistry.registerIntegerStat("sg_chests_looted", 0); //done
        StatRegistry.registerIntegerStat("sg_assists", 0);
        StatRegistry.registerIntegerStat("sg_times_mutated", 0);
        StatRegistry.registerIntegerStat("sg_mutation_kills", 0);
        StatRegistry.registerIntegerStat("sg_mutation_deaths", 0);
        StatRegistry.registerIntegerStat("sg_mutation_passes", 0);
        StatRegistry.registerIntegerStat("sg_sponsored_others", 0);
        StatRegistry.registerIntegerStat("sg_sponsor_received", 0);
        StatRegistry.registerIntegerStat("sg_tournament_kills", 0);
        StatRegistry.registerIntegerStat("sg_tournament_wins", 0);
        StatRegistry.registerIntegerStat("sg_tournament_survives", 0);
        StatRegistry.registerIntegerStat("sg_tournament_chests_looted", 0);
        StatRegistry.registerIntegerStat("sg_tournament_points", 0);
        StatRegistry.registerIntegerStat("sg_tournament_assists", 0);
        
        dataManager.setupMysql();
        serverManager.setupCurrentServer();
    
        this.socketManager = NetworkManager.create(socketContext, "127.0.0.1", 6000);
        NetworkManager.init();
    }
    
    public SocketManager getSocketManager() {
        return socketManager;
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
}
