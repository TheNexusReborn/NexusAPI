package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.RanksCodec;
import com.thenexusreborn.api.player.Preference.Info;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;

import java.util.*;
import java.util.Map.Entry;

//This is only information that needs to be accessed either right away when the player joins, or in a command if they are offline
//Nexus Player will extend from this class
@TableInfo("profiles")
public class CachedPlayer {
    @Primary 
    protected int id;
    
    @ColumnInfo 
    protected UUID uniqueId;
    
    protected String name;
    
    @ColumnInfo(codec = RanksCodec.class) 
    protected Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
    
    @ColumnIgnored 
    protected Map<String, Preference> preferences = new HashMap<>();
    
    @ColumnIgnored
    protected Map<String, Stat> stats = new HashMap<>();
    
    @ColumnIgnored
    protected Set<StatChange> statChanges = new TreeSet<>();
    
    public CachedPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public CachedPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }
    
    public CachedPlayer(CachedPlayer cachedPlayer) {
        //Benefit of doing the direct fields is that it keeps the same reference, instead of creating a copy, better performance
        this.id = cachedPlayer.id;
        this.uniqueId = cachedPlayer.uniqueId;
        this.name = cachedPlayer.name;
        this.ranks = cachedPlayer.ranks;
        this.preferences = cachedPlayer.preferences;
        this.stats = cachedPlayer.stats;
        this.statChanges = cachedPlayer.statChanges;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Map<Rank, Long> getRanks() {
        return new EnumMap<>(ranks);
    }
    
    public Map<String, Preference> getPreferences() {
        return new HashMap<>(preferences);
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
            Info info = NexusAPI.getApi().getDataManager().getPreferenceInfo().get(name.toLowerCase());
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
    
    public void addRank(Rank rank, long expire) throws Exception {
        if (rank == Rank.NEXUS) {
            throw new Exception("Cannot add the Nexus Team rank.");
        }
        
        if (System.currentTimeMillis() > expire) {
            throw new Exception("Cannot add the rank as it has already expired.");
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
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean hasStat(String statName) {
        return this.stats.containsKey(statName);
    }
    
    public void addStat(Stat stat) {
        this.stats.put(stat.getName(), stat);
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
}
