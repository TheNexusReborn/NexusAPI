package com.thenexusreborn.api;

import com.thenexusreborn.api.gamearchive.GameAction;
import com.thenexusreborn.api.gamearchive.GameInfo;
import com.thenexusreborn.api.levels.LevelManager;
import com.thenexusreborn.api.maven.MavenLibrary;
import com.thenexusreborn.api.network.NetworkContext;
import com.thenexusreborn.api.network.NetworkManager;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.nickname.Nickname;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.Punishment;
import com.thenexusreborn.api.punishment.PunishmentManager;
import com.thenexusreborn.api.registry.DatabaseRegistry;
import com.thenexusreborn.api.registry.NetworkCommandRegistry;
import com.thenexusreborn.api.registry.StatRegistry;
import com.thenexusreborn.api.registry.ToggleRegistry;
import com.thenexusreborn.api.server.*;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;
import com.thenexusreborn.api.tags.Tag;
import com.thenexusreborn.api.tags.TagRegistry;
import me.firestar311.starclock.api.ClockManager;
import me.firestar311.starlib.api.Value;
import me.firestar311.starlib.api.scheduler.Scheduler;
import me.firestar311.starsql.api.objects.Row;
import me.firestar311.starsql.api.objects.SQLDatabase;
import me.firestar311.starsql.api.objects.typehandlers.ValueHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.30")
@MavenLibrary(groupId = "javax.xml.bind", artifactId = "jaxb-api", version = "2.3.1")
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
    protected final NetworkManager networkManager;
    protected final PunishmentManager punishmentManager;
    protected final LevelManager levelManager;
    protected ClockManager clockManager;
    protected Version version;
    protected Scheduler scheduler;
    
    protected StatRegistry statRegistry;
    protected ToggleRegistry toggleRegistry;
    protected TagRegistry tagRegistry;
    protected DatabaseRegistry databaseRegistry;
    
    protected Map<UUID, PrivateAlphaUser> privateAlphaUsers = new HashMap<>();
    
    protected SQLDatabase primaryDatabase;
    
    public NexusAPI(Environment environment, NetworkContext context, Logger logger, PlayerManager playerManager, Scheduler scheduler, ServerManager serverManager) {
        this.logger = logger;
        this.environment = environment;
        this.networkManager = new NetworkManager(context);
        this.playerManager = playerManager;
        this.serverManager = serverManager;
        this.punishmentManager = new PunishmentManager();
        this.levelManager = new LevelManager();
        
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
        
        NetworkCommandRegistry networkCommandRegistry = new NetworkCommandRegistry();
        registerNetworkCommands(networkCommandRegistry);
        networkCommandRegistry.register("updaterank", new NetworkCommand("updaterank", (cmd, origin, args) -> {
            UUID uuid = UUID.fromString(args[0]);
            String action = args[1];
            Rank rank = Rank.parseRank(args[2]);
            long expire = args.length > 3 ? Long.parseLong(args[3]) : -1;
            
            NexusProfile profile = getPlayerManager().getProfile(uuid);
            
            if (profile != null) {
                switch (action) {
                    case "add" -> profile.addRank(rank, expire);
                    case "remove" -> profile.removeRank(rank);
                    case "set" -> profile.setRank(rank, expire);
                }
            }
        }));
        
        networkCommandRegistry.register("updatetag", new NetworkCommand("updatetag", (cmd, origin, args) -> {
            UUID uuid = UUID.fromString(args[0]);
            String action = args[1];
            String tag = args.length > 2 ? args[2] : "";
            NexusPlayer player = playerManager.getNexusPlayer(uuid);
            if (action.equalsIgnoreCase("reset")) {
                if (player != null) {
                    player.getTags().setActive(null);
                }
            } else if (action.equalsIgnoreCase("set")) {
                if (player != null) {
                    player.getTags().setActive(tag);
                }
            } else {
                if (player == null) {
                    player = playerManager.getCachedPlayer(uuid).loadFully();
                }
                
                if (action.equalsIgnoreCase("unlock")) {
                    long timestamp = Long.parseLong(args[3]);
                    player.getTags().add(new Tag(player.getUniqueId(), tag, timestamp));
                } else if (action.equalsIgnoreCase("remove")) {
                    player.getTags().remove(tag);
                }
            }
        }));
    
        networkCommandRegistry.register("updatestat", new NetworkCommand("updatestat", (cmd, origin, args) -> {
            if (getServerManager().getCurrentServer().getName().equalsIgnoreCase(origin)) {
                return;
            }
            UUID uuid = UUID.fromString(args[0]);
            Stat.Info info = StatHelper.getInfo(args[1]);
            StatOperator operator = StatOperator.valueOf(args[2]);
            Object value = new ValueHandler().getDeserializer().deserialize(null, args[3]);
            NexusProfile profile = NexusAPI.getApi().getPlayerManager().getProfile(uuid);
            StatChange statChange = new StatChange(info, uuid, value, operator, System.currentTimeMillis());
            profile.addStatChange(statChange);
        }));
        
        networkCommandRegistry.register("updateprivatealpha", new NetworkCommand("updateprivatealpha", (cmd, origin, args) -> {
            String action = args[0];
            if (action.equalsIgnoreCase("add")) {
                long id = Long.parseLong(args[1]);
                UUID uuid = UUID.fromString(args[2]);
                String name = args[3];
                long timestamp = Long.parseLong(args[4]);
                this.privateAlphaUsers.put(uuid, new PrivateAlphaUser(id, uuid, name, timestamp));
            } else if (action.equalsIgnoreCase("remove")) {
                UUID uuid = UUID.fromString(args[1]);
                this.privateAlphaUsers.remove(uuid);
            }
        }));
        
        networkCommandRegistry.register("playercreate", new NetworkCommand("playercreate", (cmd, origin, args) -> {
            UUID uuid = UUID.fromString(args[0]);
            try {
                NexusPlayer nexusPlayer = getPrimaryDatabase().get(NexusPlayer.class, "uniqueId", uuid).get(0);
                getPlayerManager().getCachedPlayers().put(nexusPlayer.getUniqueId(), new CachedPlayer(nexusPlayer));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
        
        networkManager.init("localhost", 3408);
        for (NetworkCommand netCmd : networkCommandRegistry.getRegisteredObjects().values()) {
            networkManager.addCommand(netCmd);
        }
        getLogger().info("Loaded the Networking System");
        
        databaseRegistry = new DatabaseRegistry(logger);
        registerDatabases(databaseRegistry);
        getLogger().info("Registered the databases");
        
        for (SQLDatabase database : databaseRegistry.getRegisteredObjects().values()) {
            if (database.getName().toLowerCase().contains("nexus")) { //TODO Temporary
                database.registerClass(IPEntry.class);
                database.registerClass(Stat.Info.class);
                database.registerClass(Stat.class);
                database.registerClass(StatChange.class);
                database.registerClass(Toggle.Info.class);
                database.registerClass(Toggle.class);
                database.registerClass(NexusPlayer.class);
                database.registerClass(ServerInfo.class);
                database.registerClass(GameInfo.class);
                database.registerClass(GameAction.class);
                database.registerClass(Punishment.class);
                database.registerClass(Nickname.class);
                database.registerClass(Tag.class);
                database.registerClass(PrivateAlphaUser.class);
                database.registerClass(Session.class);
                this.primaryDatabase = database;
            }
        }
        
        if (primaryDatabase == null) {
            throw new SQLException("Could not find the primary database.");
        }
        
        databaseRegistry.setup();
        getLogger().info("Successfully setup the database tables");
        
        statRegistry = StatHelper.getRegistry();
        
        List<Stat.Info> statInfos = primaryDatabase.get(Stat.Info.class);
        
        for (Info statInfo : statInfos) {
            statRegistry.register(statInfo.getName(), statInfo);
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
        statRegistry.register("beta", "Beta", StatType.BOOLEAN, false);
        statRegistry.register("tag", "Tag", StatType.STRING, "null");
        statRegistry.register("online", "Online", StatType.BOOLEAN, false);
        statRegistry.register("server", "Server", StatType.STRING, "null");
        registerStats(statRegistry);
        
        for (Stat.Info statInfo : StatHelper.getRegistry().getRegisteredObjects().values()) {
            getPrimaryDatabase().saveSilent(statInfo);
        }
        
        getLogger().info("Pushed stat types to the database");
        getLogger().info("Registered Stat types");
        
        toggleRegistry = new ToggleRegistry();
    
        List<Toggle.Info> toggleInfos = primaryDatabase.get(Toggle.Info.class);
        for (Toggle.Info toggleInfo : toggleInfos) {
            toggleRegistry.register(toggleInfo.getName(), toggleInfo);
        }
    
        toggleRegistry.register("vanish", Rank.HELPER, "Vanish", "A staff only thing where you can be completely invisible", false);
        toggleRegistry.register("incognito", Rank.MEDIA, "Incognito", "A media+ thing where you can be hidden from others", false);
        toggleRegistry.register("fly", Rank.DIAMOND, "Fly", "A donor perk that allows you to fly in hubs and lobbies", false);
        
        registerToggles(toggleRegistry);
        getLogger().info("Registered toggle types");
        for (Toggle.Info object : toggleRegistry.getRegisteredObjects().values()) {
            getPrimaryDatabase().saveSilent(object);
        }
        getLogger().info("Pushed toggle types to the database");

        getLogger().info("Registering and Setting up Tags");
        this.tagRegistry = new TagRegistry();
        String[] defaultTags = {"thicc", "son", "e-girl", "god", "e-dater", "lord", "epic", "bacca", "benja", "milk man", "champion"};
        for (String dt : defaultTags) {
            this.tagRegistry.register(dt, dt);
        }
        getLogger().info("Registered " + this.tagRegistry.getRegisteredObjects().size() + " default tags.");

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
            CachedPlayer cachedPlayer = new CachedPlayer(row.getLong("id"), uniqueId, row.getString("name"));
            cachedPlayer.getRanks().setAll((PlayerRanks) row.getObject("ranks"));
            playerManager.getCachedPlayers().put(cachedPlayer.getUniqueId(), cachedPlayer);
        }
        getLogger().info("Loaded basic player data (database IDs, Unique IDs and Names) - " + playerManager.getCachedPlayers().size() + " total profiles.");
        
        List<Row> statsRows = database.executeQuery("select `name`,`uuid`,`value` from stats where `name` in ('server','online','lastlogout','privatealpha');");
        for (Row row : statsRows) {
            String name = row.getString("name");
            UUID uuid = UUID.fromString(row.getString("uuid"));
            Stat.Info info = StatHelper.getInfo(name);
            Value value = (Value) row.getObject("value");
            CachedPlayer player = playerManager.getCachedPlayer(uuid);
            if (player == null) {
                continue;
            }
            if (name.equalsIgnoreCase("server")) {
                player.setServer(value.getAsString());
            } else if (name.equalsIgnoreCase("online")) {
                player.setOnline(value.getAsBoolean());
            } else if (name.equalsIgnoreCase("lastlogout")) {
                player.setLastLogout(value.getAsLong());
            }
        }
        getLogger().info("Loaded stats for player profiles: Current Server, Online Status, and Last Logout time");
    
        List<Toggle> toggles = database.get(Toggle.class);
        for (Toggle toggle : toggles) {
            CachedPlayer cachedPlayer = playerManager.getCachedPlayers().get(toggle.getUuid());
            if (cachedPlayer == null) {
                continue;
            }
    
            cachedPlayer.addToggle(toggle);
        }

        getLogger().info("Loaded toggle info for player profiles: Incognito, Vanish and Fly");

        List<Tag> allTags = database.get(Tag.class);
        Map<UUID, List<Tag>> sortedTags = new HashMap<>();
        for (Tag tag : allTags) {
            if (sortedTags.containsKey(tag.getUuid())) {
                sortedTags.get(tag.getUuid()).add(tag);
            } else {
                sortedTags.put(tag.getUuid(), new ArrayList<>(Collections.singletonList(tag)));
            }
        }

        sortedTags.forEach((uuid, tags) -> {
            NexusProfile profile = playerManager.getProfile(uuid);
            profile.getTags().addAll(tags);
        });
        getLogger().info("Loaded all tag settings for players.");

        for (IPEntry entry : playerManager.getIpHistory()) {
            CachedPlayer player = playerManager.getCachedPlayer(entry.getUuid());
            if (player != null) {
                player.getIpHistory().add(entry);
            }
        }
        getLogger().info("Sorted IP History for player profiles.");
    
        List<PrivateAlphaUser> privateAlphaUsers = getPrimaryDatabase().get(PrivateAlphaUser.class);
        for (PrivateAlphaUser pau : privateAlphaUsers) {
            this.privateAlphaUsers.put(pau.getUuid(), pau);
        }
    
        getLogger().info("NexusAPI v" + this.version + " load complete.");
    }
    
    public abstract void registerDatabases(DatabaseRegistry registry);
    
    public abstract void registerStats(StatRegistry registry);
    
    public abstract void registerNetworkCommands(NetworkCommandRegistry registry);
    
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
    
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    public Logger getLogger() {
        return logger;
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
    
    public Map<UUID, PrivateAlphaUser> getPrivateAlphaUsers() {
        return privateAlphaUsers;
    }

    public ClockManager getClockManager() {
        return clockManager;
    }

    public void setClockManager(ClockManager clockManager) {
        this.clockManager = clockManager;
    }
}
