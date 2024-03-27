package com.thenexusreborn.api;

import com.stardevllc.starclock.ClockManager;
import com.stardevllc.starlib.task.TaskFactory;
import com.thenexusreborn.api.experience.LevelManager;
import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.gamearchive.GameAction;
import com.thenexusreborn.api.gamearchive.GameInfo;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.player.PlayerManager.Name;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentManager;
import com.thenexusreborn.api.registry.StatRegistry;
import com.thenexusreborn.api.registry.ToggleRegistry;
import com.thenexusreborn.api.server.*;
import com.thenexusreborn.api.sql.DatabaseRegistry;
import com.thenexusreborn.api.sql.objects.Row;
import com.thenexusreborn.api.sql.objects.SQLDatabase;
import com.thenexusreborn.api.stats.Stat;
import com.thenexusreborn.api.stats.StatChange;
import com.thenexusreborn.api.stats.StatHelper;
import com.thenexusreborn.api.stats.StatType;
import com.thenexusreborn.api.sql.objects.codecs.RanksCodec;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.tags.TagRegistry;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NexusAPI {
    private static NexusAPI instance;
    public static final Phase PHASE = Phase.PRIVATE_ALPHA;
    public static final NetworkType NETWORK_TYPE = NetworkType.SINGLE;

    public static void setApi(NexusAPI api) {
        instance = api;
    }

    public static NexusAPI getApi() {
        return instance;
    }

    protected final Logger logger;
    protected final PlayerManager playerManager;
    protected final ServerManager serverManager;
    protected final Environment environment;
    protected final PunishmentManager punishmentManager;
    protected final LevelManager levelManager;
    protected ClockManager clockManager;
    protected Version version;
    protected TaskFactory scheduler;

    protected StatRegistry statRegistry;
    protected ToggleRegistry toggleRegistry;
    protected TagRegistry tagRegistry;
    protected DatabaseRegistry databaseRegistry;

    protected SQLDatabase primaryDatabase;

    public NexusAPI(Environment environment, Logger logger, PlayerManager playerManager, TaskFactory scheduler, ServerManager serverManager) {
        this.logger = logger;
        this.environment = environment;
        this.playerManager = playerManager;
        this.serverManager = serverManager;
        this.punishmentManager = new PunishmentManager();
        this.levelManager = new LevelManager();
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

        databaseRegistry = new DatabaseRegistry(logger);

        registerDatabases(databaseRegistry);
        getLogger().info("Registered the databases");

        for (SQLDatabase database : databaseRegistry.getObjects().values()) {
            if (database.getName().toLowerCase().contains("nexus")) {
                database.registerClass(PlayerExperience.class);
                database.registerClass(PlayerTime.class);
                database.registerClass(PlayerBalance.class);
                database.registerClass(IPEntry.class);
                database.registerClass(Stat.class);
                database.registerClass(StatChange.class);
                database.registerClass(Toggle.class);
                database.registerClass(NexusPlayer.class);
                database.registerClass(ServerInfo.class);
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

        boolean migrate = false;
        File migrationFile = new File("." + File.separator + "nexusmigration");
        if (!migrationFile.exists()) {
            migrate = true;
            migrationFile.createNewFile();
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(migrationFile)))) {
                String line = br.readLine();
                if (line == null || !line.equals("1.12-ALPHA")) {
                    migrate = true;
                }
            }
        }

        databaseRegistry.setup();
        getLogger().info("Successfully setup the database tables");

        statRegistry = StatHelper.getRegistry();

        statRegistry.register("xp", "Experience", StatType.DOUBLE, 0.0); //TODO Remove after migration
        statRegistry.register("level", "Level", StatType.INTEGER, 0); //TODO Remove after migration
        statRegistry.register("nexites", "Nexites", StatType.DOUBLE, 0.0); //TODO Remove after migration
        statRegistry.register("credits", "Credits", StatType.DOUBLE, 0.0); //TODO Remove after migration
        statRegistry.register("playtime", "Playtime", StatType.LONG, 0L); //TODO Remove after migration
        statRegistry.register("firstjoined", "First Joined", StatType.LONG, 0L); //TODO Remove after migration
        statRegistry.register("lastlogin", "Last Login", StatType.LONG, 0L); //TODO Remove after migration
        statRegistry.register("lastlogout", "Last Logout", StatType.LONG, 0L); //TODO Remove after migration
        statRegistry.register("tag", "Tag", StatType.STRING, "null"); //TODO Remove after migration

        int initialStatSize = statRegistry.getObjects().size();
        getLogger().info("Registered " + initialStatSize + " default stat types.");

        registerStats(statRegistry);
        getLogger().info("Registered " + (statRegistry.getObjects().size() - initialStatSize) + " additional default stat types from other plugins.");

        toggleRegistry = new ToggleRegistry();

        toggleRegistry.register("vanish", Rank.HELPER, "Vanish", "A staff only thing where you can be completely invisible", false);
        toggleRegistry.register("incognito", Rank.MEDIA, "Incognito", "A media+ thing where you can be hidden from others", false);
        toggleRegistry.register("fly", Rank.DIAMOND, "Fly", "A donor perk that allows you to fly in hubs and lobbies", false);

        int initialToggleSize = toggleRegistry.getObjects().size();
        getLogger().info("Registered " + initialToggleSize + " default toggle types.");

        registerToggles(toggleRegistry);
        getLogger().info("Registered " + (toggleRegistry.getObjects().size() - initialToggleSize) + " additional default toggle types.");

        getLogger().info("Registering and Setting up Tags");
        this.tagRegistry = new TagRegistry();
        String[] defaultTags = {"thicc", "son", "e-girl", "god", "e-dater", "lord", "epic", "bacca", "benja", "milk man", "champion"};
        for (String dt : defaultTags) {
            this.tagRegistry.register(dt, dt);
        }
        getLogger().info("Registered " + this.tagRegistry.getObjects().size() + " default tags.");

        serverManager.setupCurrentServer();
        getLogger().info("Set up the current server");

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

        if (database.count(StatChange.class) > 0) {
            getLogger().info("Found stat changes that have not been processed, processing them now...");

            Set<String> rawUuids = new HashSet<>();

            try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select `uuid` from `statchanges`;");
                while (resultSet.next()) {
                    rawUuids.add(resultSet.getString("uuid"));
                }
            }

            getLogger().info("  Found a total of " + rawUuids.size() + " players that need to have stats processed.");

            int totalSize = rawUuids.size();

            int tenDemonination;
            if (totalSize < 10) {
                tenDemonination = 10;
            } else {
                tenDemonination = Math.min(1, totalSize / 10);
            }

            int totalProcessed = 0, processed = 0;
            for (String rawUuid : rawUuids) {
                List<NexusPlayer> players = database.get(NexusPlayer.class, "uniqueid", UUID.fromString(rawUuid));
                if (players.size() != 1) {
                    continue;
                }

                NexusPlayer player = players.get(0);
                StatHelper.consolidateStats(player);
                player.clearStatChanges();
                database.save(player);
                totalProcessed++;
                processed++;

                if (processed >= tenDemonination) {
                    getLogger().info("      Processed " + totalProcessed + " out of " + totalSize);
                    processed = 0;
                }
            }

            int leftOver = database.count(StatChange.class);
            if (leftOver == 0) {
                getLogger().info("Processing complete, all statchanges have been processed.");
            } else {
                getLogger().info("There are " + leftOver + " stat changes that were not processed.");
            }
        }

        if (migrate) {
            getLogger().info("Detected the need to migrate...");
            try (Connection connection = this.primaryDatabase.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS `nicknames`, `sggamesettings`, `sglobbysettings`, `sgsettinginfo`, `statinfo`, `toggleinfo`;");
                getLogger().info("Dropped the nicknames, sggamesettings, sglobbysettings, sgsettinginfo, statinfo and toggleinfo tables.");
                statement.execute("DELETE FROM `stats` WHERE `name`='prealpha' OR `name`='alpha' OR `name`='beta' OR `name`='online' OR `name`='server';");
                getLogger().info("Cleared the stats table of unused stats.");
                statement.execute("DELETE FROM `statchanges` WHERE `name`='prealpha' OR `name`='alpha' OR `name`='beta' OR `name`='online' OR `name`='server';");
                getLogger().info("Cleared the statchanges table of unused stats.");
                statement.execute("ALTER TABLE `players` ADD COLUMN `activetag` varchar(32);");
                getLogger().info("Added the activetag column to the players table");

                Map<UUID, PlayerExperience> playerExperiences = new HashMap<>();
                for (PlayerExperience exp : this.primaryDatabase.get(PlayerExperience.class)) {
                    playerExperiences.put(exp.getUniqueId(), exp);
                }

                Map<UUID, PlayerTime> playerTimes = new HashMap<>();
                for (PlayerTime pt : this.primaryDatabase.get(PlayerTime.class)) {
                    playerTimes.put(pt.getUniqueId(), pt);
                }
                
                Map<UUID, PlayerBalance> playerBalances = new HashMap<>();
                for (PlayerBalance pb : this.primaryDatabase.get(PlayerBalance.class)) {
                    playerBalances.put(pb.getUniqueId(), pb);
                }

                ResultSet statsSet = statement.executeQuery("SELECT `uuid`, `name`, `value` FROM `stats` WHERE `name`='xp' OR `name`='level' OR `name`='playtime' OR `name`='firstjoined' OR `name`='lastlogin' OR `name`='lastlogout' OR `name`='credits' OR `name`='nexites';");
                while (statsSet.next()) {
                    UUID uuid = UUID.fromString(statsSet.getString("uuid"));
                    PlayerExperience experience = playerExperiences.computeIfAbsent(uuid, PlayerExperience::new);
                    PlayerTime playerTime = playerTimes.computeIfAbsent(uuid, PlayerTime::new);
                    PlayerBalance playerBalance = playerBalances.computeIfAbsent(uuid, PlayerBalance::new);

                    String statName = statsSet.getString("name");
                    String value = statsSet.getString("value").split(":")[1];
                    if (statName.equalsIgnoreCase("xp")) {
                        experience.setLevelXp(Double.parseDouble(value));
                    } else if (statName.equalsIgnoreCase("level")) {
                        experience.setLevel(Integer.parseInt(value));
                    } else if (statName.equalsIgnoreCase("playtime")) {
                        playerTime.setPlaytime(Long.parseLong(value));
                    } else if (statName.equalsIgnoreCase("firstjoined")) {
                        playerTime.setFirstJoined(Long.parseLong(value));
                    } else if (statName.equalsIgnoreCase("lastlogin")) {
                        playerTime.setLastLogin(Long.parseLong(value));
                    } else if (statName.equalsIgnoreCase("lastlogout")) {
                        playerTime.setLastLogout(Long.parseLong(value));
                    } else if (statName.equalsIgnoreCase("credits")) {
                        playerBalance.setCredits(Double.parseDouble(value));
                    } else if (statName.equalsIgnoreCase("nexites")) {
                        playerBalance.setNexites(Double.parseDouble(value));
                    }
                    NexusPlayer player = playerManager.getNexusPlayer(uuid);
                    if (player != null) {
                        player.getStats().clear();
                    }
                }

                for (PlayerExperience exp : playerExperiences.values()) {
                    this.primaryDatabase.save(exp);
                }
                getLogger().info("Moved experience stats to the new experience table");

                for (PlayerTime pt : playerTimes.values()) {
                    this.primaryDatabase.save(pt);
                }
                getLogger().info("Moved the player time stats to the new playertimes table");

                for (PlayerBalance pb : playerBalances.values()) {
                    this.primaryDatabase.save(pb);
                }
                getLogger().info("Moved the player balance stats to the new balances table");

                statement.execute("DELETE FROM `stats` WHERE `name`='xp' OR `name`='level' OR `name`='playtime' OR `name`='firstjoined' OR `name`='lastlogin'  OR `name`='lastlogout' OR `name`='credits' OR `name`='nexites' OR `name`='tag';");
                getLogger().info("Cleared the stats table of the xp, level, playtime, firstjoined, lastlogin, lastlogout, credits, nexites and tag stat types.");
                statement.execute("DELETE FROM `statchanges` WHERE `name`='xp' OR `name`='level' OR `name`='playtime' OR `name`='firstjoined' OR `name`='lastlogin'  OR `name`='lastlogout' OR `name`='credits' OR `name`='nexites' OR `name`='tag';");
                getLogger().info("Cleared the statchanges table of the xp, level, playtime, firstjoined, lastlogin, lastlogout, credits, nexites and tag stat types.");
            }

            try (FileWriter fileWriter = new FileWriter(migrationFile)) {
                fileWriter.write(this.version.toString());
                fileWriter.flush();
            }
            getLogger().info("Migration complete.");
        }

        getLogger().info("NexusAPI v" + this.version + " load complete.");
    }

    public abstract void registerDatabases(DatabaseRegistry registry);

    public abstract void registerStats(StatRegistry registry);

    public abstract void registerToggles(ToggleRegistry registry);

    public Version getVersion() {
        return version;
    }

    @Deprecated
    public abstract Connection getConnection() throws SQLException;

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

    public ServerManager getServerManager() {
        return serverManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
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

    public StatRegistry getStatRegistry() {
        return statRegistry;
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
}
