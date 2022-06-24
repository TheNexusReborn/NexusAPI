package com.thenexusreborn.api;

import com.thenexusreborn.api.data.DataManager;
import com.thenexusreborn.api.network.*;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.*;
import com.thenexusreborn.api.server.ServerManager;
import com.thenexusreborn.api.thread.ThreadFactory;
import com.thenexusreborn.api.tournament.Tournament;

import java.sql.*;
import java.util.List;
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
    
    public void init() throws Exception {
        try {
            Driver mysqlDriver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(mysqlDriver);
        } catch (SQLException e) {
            getLogger().severe("Error while loading the MySQL driver, disabling plugin");
            e.printStackTrace();
            return;
        }
        
//        StatHelper.registerDoubleStat("nexites", 0); //done
//        StatHelper.registerDoubleStat("credits", 0); //done
//        StatHelper.registerDoubleStat("xp", 0); //done
//        StatHelper.registerIntegerStat("sg_score", 100); //done
//        StatHelper.registerIntegerStat("sg_kills", 0); //done
//        StatHelper.registerIntegerStat("sg_highest_kill_streak", 0); //done
//        StatHelper.registerIntegerStat("sg_games", 0); //done
//        StatHelper.registerIntegerStat("sg_win_streak", 0); //done
//        StatHelper.registerIntegerStat("sg_wins", 0); //done
//        StatHelper.registerIntegerStat("sg_deaths", 0); //done
//        StatHelper.registerIntegerStat("sg_deathmatches_reached", 0); //done
//        StatHelper.registerIntegerStat("sg_chests_looted", 0); //done
//        StatHelper.registerIntegerStat("sg_assists", 0);
//        StatHelper.registerIntegerStat("sg_times_mutated", 0);
//        StatHelper.registerIntegerStat("sg_mutation_kills", 0);
//        StatHelper.registerIntegerStat("sg_mutation_deaths", 0);
//        StatHelper.registerIntegerStat("sg_mutation_passes", 0);
//        StatHelper.registerIntegerStat("sg_sponsored_others", 0);
//        StatHelper.registerIntegerStat("sg_sponsor_received", 0);
//        StatHelper.registerIntegerStat("sg_tournament_kills", 0);
//        StatHelper.registerIntegerStat("sg_tournament_wins", 0);
//        StatHelper.registerIntegerStat("sg_tournament_survives", 0);
//        StatHelper.registerIntegerStat("sg_tournament_chests_looted", 0);
//        StatHelper.registerIntegerStat("sg_tournament_points", 0);
//        StatHelper.registerIntegerStat("sg_tournament_assists", 0);
        
        dataManager.setupMysql();
        serverManager.setupCurrentServer();
        networkManager.init("localhost", 3408);
        
        networkManager.addCommand(new NetworkCommand("tournament", ((cmd, origin, args) -> {
            if (args[0].equalsIgnoreCase("delete")) {
                setTournament(null);
    
                for (NexusPlayer player : NexusAPI.getApi().getPlayerManager().getPlayers().values()) {
                    player.getStats().values().removeIf(stat -> stat.getName().contains("tournament"));
                    player.getStatChanges().removeIf(statChange -> statChange.getStatName().contains("tournament"));
                }
            } else {
                System.out.println("Received request to update in memory tournament info");
                int id = Integer.parseInt(args[0]);
                System.out.println("Tournament id is " + id);
                Tournament tournament = NexusAPI.getApi().getDataManager().getTournament(id);
                System.out.println("Tournament Info: " + tournament);
                setTournament(tournament);
            }
        })));
        
        networkManager.addCommand(new NetworkCommand("punishment"));
        networkManager.addCommand(new NetworkCommand("removepunishment", (cmd, origin, args) -> {
            int id = Integer.parseInt(args[0]);
            Punishment punishment = NexusAPI.getApi().getPunishmentManager().getPunishment(id);
            if (punishment != null) {
                punishment = NexusAPI.getApi().getDataManager().getPunishment(id);
                NexusAPI.getApi().getPunishmentManager().addPunishment(punishment);
            }
        }));
        
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
}
