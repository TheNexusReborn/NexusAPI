package com.thenexusreborn.api;

import com.stardevllc.starlib.clock.ClockManager;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starlib.task.TaskFactory;
import com.thenexusreborn.api.experience.LevelManager;
import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.gamearchive.GameAction;
import com.thenexusreborn.api.gamearchive.GameInfo;
import com.thenexusreborn.api.gamearchive.GameLogExporter;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.player.PlayerManager.Name;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentManager;
import com.thenexusreborn.api.registry.ToggleRegistry;
import com.thenexusreborn.api.server.NexusServer;
import com.thenexusreborn.api.server.ServerRegistry;
import com.thenexusreborn.api.sql.DatabaseRegistry;
import com.thenexusreborn.api.sql.objects.Row;
import com.thenexusreborn.api.sql.objects.SQLDatabase;
import com.thenexusreborn.api.sql.objects.codecs.RanksCodec;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.util.Environment;
import com.thenexusreborn.api.util.NetworkType;
import com.thenexusreborn.api.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NexusAPI {
    private static NexusAPI instance;
    public static final NetworkType NETWORK_TYPE = NetworkType.SINGLE;

    public static void setApi(NexusAPI api) {
        instance = api;
    }

    public static NexusAPI getApi() {
        return instance;
    }

    protected final Logger logger;
    protected final PlayerManager playerManager;
    protected final Environment environment;
    protected final PunishmentManager punishmentManager;
    protected final LevelManager levelManager;
    protected ClockManager clockManager;
    protected Version version;
    protected TaskFactory scheduler;
    
    protected ServerRegistry<NexusServer> serverRegistry;
    protected ToggleRegistry toggleRegistry;
    protected StringRegistry<String> tagRegistry;
    protected DatabaseRegistry databaseRegistry;

    protected SQLDatabase primaryDatabase;
    protected GameLogExporter gameLogExporter;

    public NexusAPI(Environment environment, Logger logger, PlayerManager playerManager, TaskFactory scheduler) {
        this.logger = logger;
        this.environment = environment;
        this.playerManager = playerManager;
        this.punishmentManager = new PunishmentManager();
        this.levelManager = new LevelManager();
        this.levelManager.init();
        this.scheduler = scheduler;

        URL url = NexusAPI.class.getClassLoader().getResource("nexusapi-version.txt");
        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
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

        serverRegistry = new ServerRegistry<>();
        databaseRegistry = new DatabaseRegistry(logger);

        registerDatabases(databaseRegistry);
        getLogger().info("Registered the databases");

        for (SQLDatabase database : databaseRegistry.getObjects().values()) {
            if (database.getName().toLowerCase().contains("nexus")) {
                database.registerClass(PlayerExperience.class);
                database.registerClass(PlayerTime.class);
                database.registerClass(PlayerBalance.class);
                database.registerClass(IPEntry.class);
                database.registerClass(Toggle.class);
                database.registerClass(NexusPlayer.class);
                database.registerClass(GameInfo.class);
                database.registerClass(GameAction.class);
                database.registerClass(Punishment.class);
                database.registerClass(Tag.class);
                database.registerClass(Session.class);
                this.primaryDatabase = database;
            }
        }

        if (primaryDatabase == null) {
            throw new SQLException("Could not find the primary database.");
        }

        databaseRegistry.setup();
        getLogger().info("Successfully setup the database tables");

        toggleRegistry = new ToggleRegistry();

        toggleRegistry.register("vanish", Rank.HELPER, "Vanish", "A staff only thing where you can be completely invisible", false);
        toggleRegistry.register("incognito", Rank.MEDIA, "Incognito", "A media+ thing where you can be hidden from others", false);
        toggleRegistry.register("fly", Rank.DIAMOND, "Fly", "A donor perk that allows you to fly in hubs and lobbies", false);

        int initialToggleSize = toggleRegistry.getObjects().size();
        getLogger().info("Registered " + initialToggleSize + " default toggle types.");

        registerToggles(toggleRegistry);
        getLogger().info("Registered " + (toggleRegistry.getObjects().size() - initialToggleSize) + " additional default toggle types.");

        getLogger().info("Registering and Setting up Tags");
        this.tagRegistry = new StringRegistry<>();
        String[] defaultTags = {"thicc", "son", "e-girl", "god", "e-dater", "lord", "epic", "bacca", "benja", "milk man", "champion"};
        for (String dt : defaultTags) {
            this.tagRegistry.register(dt, dt);
        }
        getLogger().info("Registered " + this.tagRegistry.getObjects().size() + " default tags.");

        for (Punishment punishment : getPrimaryDatabase().get(Punishment.class)) {
            punishmentManager.addPunishment(punishment);
        }
        getLogger().info("Cached punishments in memory");

        playerManager.getIpHistory().addAll(getPrimaryDatabase().get(IPEntry.class));
        getLogger().info("Loaded IP History");

        SQLDatabase database = getPrimaryDatabase();
        List<Row> playerRows = database.executeQuery("select * from players;");

        for (Row row : playerRows) {
            UUID uniqueId = (UUID) row.getObject("uniqueId");
            String name = row.getString("name");
            PlayerRanks playerRanks = new RanksCodec().decode(row.getString("ranks"));
            playerManager.getUuidNameMap().put(uniqueId, new Name(name));
            playerManager.getUuidRankMap().put(uniqueId, playerRanks);
        }
        getLogger().info("Loaded basic player data (database IDs, Unique IDs and Names) - " + playerManager.getUuidNameMap().size() + " total profiles.");
        getLogger().info("NexusAPI v" + this.version + " load complete.");
    }

    public abstract void registerDatabases(DatabaseRegistry registry);

    public abstract void registerToggles(ToggleRegistry registry);

    public Version getVersion() {
        return version;
    }

    public abstract File getFolder();

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TaskFactory getScheduler() {
        return scheduler;
    }

    public Logger getLogger() {
        return logger;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public GameLogExporter getGameLogExporter() {
        return gameLogExporter;
    }

    public void setGameLogExporter(GameLogExporter gameLogExporter) {
        this.gameLogExporter = gameLogExporter;
    }

    public static void logMessage(Level level, String mainMessage, String... debug) {
        Logger logger = NexusAPI.getApi().getLogger();
        logger.log(level, "----------- Nexus Log -----------");
        logger.log(level, mainMessage);
        if (debug != null) {
            for (String s : debug) {
                logger.log(level, s);
            }
        }
        logger.log(level, "---------------------------------");
    }

    public SQLDatabase getPrimaryDatabase() {
        return this.primaryDatabase;
    }

    public ToggleRegistry getToggleRegistry() {
        return toggleRegistry;
    }

    public URLClassLoader getLoader() {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            return (URLClassLoader) classLoader;
        }
        return null;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public ClockManager getClockManager() {
        return clockManager;
    }

    public void setClockManager(ClockManager clockManager) {
        this.clockManager = clockManager;
    }

    public ServerRegistry<NexusServer> getServerRegistry() {
        return serverRegistry;
    }
}
