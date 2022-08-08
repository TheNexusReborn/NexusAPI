package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.RanksCodec;
import com.thenexusreborn.api.data.handler.PlayerObjectHandler;
import com.thenexusreborn.api.levels.LevelManager;
import com.thenexusreborn.api.player.Preference.Info;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;

import java.util.*;
import java.util.Map.Entry;

@TableInfo(value = "players", handler = PlayerObjectHandler.class)
public class NexusPlayer {
    
    @Primary
    protected long id;
    protected UUID uniqueId;
    protected String name;
    
    @ColumnIgnored
    protected final Set<IPEntry> ipHistory = new HashSet<>();
    
    @ColumnInfo(type = "varchar(1000)", codec = RanksCodec.class)
    protected Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
    
    @ColumnIgnored
    protected final Map<String, Preference> preferences = new HashMap<>();
    
    @ColumnIgnored
    protected NexusScoreboard scoreboard;
    
    @ColumnIgnored
    protected UUID lastMessage;
    
    @ColumnIgnored
    protected final Map<String, Stat> stats = new HashMap<>();
    
    @ColumnIgnored
    protected final Set<StatChange> statChanges = new TreeSet<>();
    
    @ColumnIgnored
    protected IActionBar actionBar;
    
    @ColumnIgnored
    protected boolean spokenInChat;
    
    @ColumnIgnored
    protected PlayerProxy playerProxy;
    
    private NexusPlayer() {}
    
    public NexusPlayer(UUID uniqueId) {
        this(uniqueId, "");
    }
    
    public NexusPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }
    
    public NexusScoreboard getScoreboard() {
        return scoreboard;
    }
    
    public void setScoreboard(NexusScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
    
    @Deprecated
    public long getFirstJoined() {
        return (long) getStatValue("firstjoined");
    }
    
    @Deprecated
    public void setFirstJoined(long firstJoined) {
        changeStat("firstjoined", firstJoined, StatOperator.SET);
    }
    
    @Deprecated
    public long getLastLogin() {
        return (long) getStatValue("lastlogin");
    }
    
    @Deprecated
    public void setLastLogin(long lastLogin) {
        changeStat("lastlogin", lastLogin, StatOperator.SET);
    }
    
    @Deprecated
    public void setUnlockedTags(Set<String> unlockedTags) {
        changeStat("unlockedtags", unlockedTags, StatOperator.SET);
    }
    
    public String getDisplayName() {
        if (getRank() != Rank.MEMBER) {
            return getRank().getPrefix() + " &f" + getName();
        } else {
            return getRank().getPrefix() + getName();
        }
    }
    
    public void sendMessage(String message) {
        playerProxy.sendMessage(message);
    }
    
    public String getTablistName() {
        if (getRank() == Rank.MEMBER) {
            return Rank.MEMBER.getColor() + getName();
        } else {
            return "&f" + getName();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NexusPlayer that = (NexusPlayer) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
    
    @Deprecated
    public long getLastLogout() {
        return (long) getStatValue("lastlogout");
    }
    
    @Deprecated
    public void setLastLogout(long lastLogout) {
        changeStat("lastlogout", lastLogout, StatOperator.SET);
    }
    
    public PlayerProxy getPlayer() {
        return this.playerProxy;
    }
    
    @Deprecated
    public int getLevel() {
        double xp = (double) getStatValue("xp");
        int playerLevel = 0;
        for (int i = 1; i < LevelManager.levels.size(); i++) {
            if (i == 1) {
                if (xp > LevelManager.levels.get(i)) {
                    playerLevel = i;
                    continue;
                } else {
                    break;
                }
            }
            
            if (xp >= LevelManager.levels.get(i - 1) && xp < LevelManager.levels.get(i)) {
                playerLevel = i - 1;
            }
        }
        
        return playerLevel;
    }
    
    public NexusPlayer getLastMessage() {
        return NexusAPI.getApi().getPlayerManager().getNexusPlayer(this.lastMessage);
    }
    
    public void setLastMessage(NexusPlayer nexusPlayer) {
        this.lastMessage = nexusPlayer.getUniqueId();
    }
    
    @Deprecated
    public boolean isPrealpha() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setPrealpha(boolean prealpha) {
        changeStat("prealpha", prealpha, StatOperator.SET);
    }
    
    @Deprecated
    public boolean isAlpha() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setAlpha(boolean alpha) {
        changeStat("alpha", alpha, StatOperator.SET);
    }
    
    @Deprecated
    public boolean isBeta() {
        return (boolean) getStatValue("prealpha");
    }
    
    @Deprecated
    public void setBeta(boolean beta) {
        changeStat("beta", beta, StatOperator.SET);
    }
    
    public Preference getPreference(String name) {
        return this.preferences.get(name.toLowerCase());
    }
    
    public void addPreference(Preference preference) {
        this.preferences.put(preference.getInfo().getName().toLowerCase(), preference);
    }
    
    public void setPreferenceValue(String name, boolean value) {
        Preference preference = getPreference(name);
        if (preference != null) {
            preference.setValue(value);
        }
    }
    
    public boolean getPreferenceValue(String name) {
        Preference preference = getPreference(name);
        if (preference != null) {
            return preference.getValue();
        } else {
            Info info = NexusAPI.getApi().getPreferenceRegistry().get(name.toLowerCase());
            if (info != null) {
                return info.getDefaultValue();
            } else {
                throw new IllegalArgumentException("Invalid preference name: " + name);
            }
        }
    }
    
    public void setPreferences(List<Preference> preferences) {
        this.preferences.clear();
        for (Preference preference : preferences) {
            addPreference(preference);
        }
    }
    
    public Rank getRank() {
        if (PlayerManager.NEXUS_TEAM.contains(this.uniqueId)) {
            return Rank.NEXUS;
        }
        
        for (Entry<Rank, Long> entry : new EnumMap<>(this.ranks).entrySet()) {
            if (entry.getValue() == -1) {
                return entry.getKey();
            }
            
            if (System.currentTimeMillis() <= entry.getValue()) {
                return entry.getKey();
            }
        }
        
        return Rank.MEMBER;
    }
    
    public void addRank(Rank rank, long expire) {
        if (rank == Rank.NEXUS) {
            throw new RuntimeException("Cannot add the Nexus Team rank.");
        }
        
        if (System.currentTimeMillis() > expire) {
            throw new RuntimeException("Cannot add the rank as it has already expired.");
        }
        
        this.ranks.put(rank, expire);
    }
    
    public void setRank(Rank rank, long expire) throws Exception {
        if (rank == Rank.NEXUS) {
            throw new Exception("Cannot set the Nexus Team rank.");
        }
        
        if (System.currentTimeMillis() > expire) {
            throw new Exception("Cannot set the rank as it has already expired.");
        }
        
        if (this.ranks.containsKey(Rank.NEXUS)) {
            throw new Exception("Cannot set a rank lower than The Nexus Team on a Nexus Team member.");
        }
        
        this.ranks.clear();
        this.ranks.put(rank, expire);
    }
    
    public void removeRank(Rank rank) throws Exception {
        if (rank == Rank.NEXUS) {
            throw new Exception("Cannot remove the Nexus Team rank.");
        }
        
        this.ranks.remove(rank);
    }
    
    public boolean hasStat(String statName) {
        return this.stats.containsKey(statName);
    }
    
    public void addStat(Stat stat) {
        if (stat.getName() != null) {
            this.stats.put(stat.getName(), stat);
        }
    }
    
    public void addStatChange(StatChange statChange) {
        this.statChanges.add(statChange);
    }
    
    public Set<StatChange> getStatChanges() {
        return statChanges;
    }
    
    public Map<String, Stat> getStats() {
        return stats;
    }
    
    public Object getStatValue(String statName) {
        Stat stat = getStat(statName);
        if (stat == null) {
            Stat.Info info = StatHelper.getInfo(statName);
            stat = new Stat(info, uniqueId, System.currentTimeMillis());
            this.addStat(stat);
        }
        return stat.getValue();
    }
    
    public Stat getStat(String name) {
        return this.stats.get(StatHelper.formatStatName(name));
    }
    
    public Tag getTag() {
        return new Tag((String) getStatValue("tag"));
    }
    
    public void setTag(Tag tag) {
        if (tag != null) {
            changeStat("tag", tag.getName(), StatOperator.SET);
        } else {
            changeStat("tag", "null", StatOperator.SET);
        }
    }
    
    public void changeStat(String statName, Object statValue, StatOperator operator) {
        Stat stat = getStat(statName);
        if (stat == null) {
            Stat.Info info = StatHelper.getInfo(statName);
            if (info == null) {
                NexusAPI.getApi().getLogger().warning("Could not find a stat with the name " + statName);
                return;
            }
            stat = new Stat(info, this.uniqueId, info.getDefaultValue(), System.currentTimeMillis());
            this.addStat(stat);
        }
        StatHelper.changeStat(stat, operator, statValue);
    }
    
    public String serializeRanks() {
        return new RanksCodec().encode(this.ranks);
    }
    
    public Set<String> getUnlockedTags() {
        return (Set<String>) getStatValue("unlockedtags");
    }
    
    public boolean isTagUnlocked(String tag) {
        return getUnlockedTags().contains(tag.toLowerCase());
    }
    
    public void unlockTag(String tag) {
        getUnlockedTags().add(tag.toLowerCase());
    }
    
    public void lockTag(String tag) {
        getUnlockedTags().remove(tag.toLowerCase());
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLastMessage(UUID lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
    
    public Map<String, Preference> getPreferences() {
        return preferences;
    }
    
    public Map<Rank, Long> getRanks() {
        return ranks;
    }
    
    public boolean isOnline() {
        return playerProxy.isOnline();
    }
    
    public IActionBar getActionBar() {
        return actionBar;
    }
    
    public void setActionBar(IActionBar actionBar) {
        this.actionBar = actionBar;
    }
    
    public void setSpokenInChat(boolean spokenInChat) {
        this.spokenInChat = spokenInChat;
    }
    
    public boolean hasSpokenInChat() {
        return this.spokenInChat;
    }
    
    public void setPlayerProxy(PlayerProxy playerProxy) {
        this.playerProxy = playerProxy;
    }
}
