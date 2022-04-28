package com.thenexusreborn.api;

import com.thenexusreborn.api.data.DataManager;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.server.ServerManager;
import com.thenexusreborn.api.stats.StatRegistry;
import com.thenexusreborn.api.tags.TagManager;
import com.thenexusreborn.api.thread.ThreadFactory;

import java.sql.*;
import java.util.logging.Logger;

public abstract class NexusAPI {
    private static NexusAPI instance;
    
    public static void setApi(NexusAPI api) {
        instance = api;
    }
    
    public static NexusAPI getApi() {
        return instance;
    }
    
    protected Logger logger;
    protected DataManager dataManager;
    protected TagManager tagManager;
    protected PlayerManager playerManager;
    protected ThreadFactory threadFactory;
    protected PlayerFactory playerFactory;
    protected ServerManager serverManager;
    
    public NexusAPI(Logger logger, DataManager dataManager, TagManager tagManager, PlayerManager playerManager, ThreadFactory threadFactory, PlayerFactory playerFactory, ServerManager serverManager) {
        this.logger = logger;
        this.dataManager = dataManager;
        this.tagManager = tagManager;
        this.playerManager = playerManager;
        this.threadFactory = threadFactory;
        this.playerFactory = playerFactory;
        this.serverManager = serverManager;
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
    
        StatRegistry.registerDoubleStat("nexites", 0);
        StatRegistry.registerDoubleStat("credits", 0);
        StatRegistry.registerDoubleStat("xp", 0);
        StatRegistry.registerIntegerStat("sg_score", 100);
        
        dataManager.setupMysql();
        serverManager.setupCurrentServer();
    }
    
    public abstract Connection getConnection() throws SQLException;
    
    public TagManager getTagManager() {
        return tagManager;
    }
    
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
}
