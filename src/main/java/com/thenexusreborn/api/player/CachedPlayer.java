package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.StringHelper;
import com.thenexusreborn.api.player.Preference.Info;

import java.util.*;
import java.util.Map.Entry;

//This is only information that needs to be accessed either right away when the player joins, or in a command if they are offline
//Nexus Player will extend from this class
public class CachedPlayer {
    protected int id; //This is the database id
    protected UUID uniqueId;
    protected String name;
    
    protected Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
    protected Map<String, Preference> preferences = new HashMap<>();
    protected Set<String> unlockedTags = new HashSet<>();
    
    //These will be udpated by the network for when a player switches servers or goes offline as these fields will be used for several features like friends, guilds and messaging
    protected boolean online;
    protected String server;
    
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
        this.unlockedTags = cachedPlayer.unlockedTags;
        this.online = cachedPlayer.online;
        this.server = cachedPlayer.server;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public String serializeRanks() {
        StringBuilder sb = new StringBuilder();
        
        if (PlayerManager.NEXUS_TEAM.contains(this.uniqueId)) {
            return Rank.NEXUS.name() + "=-1";
        }
        
        if (getRanks().size() == 0) {
            return Rank.MEMBER.name() + "=-1";
        }
        
        for (Entry<Rank, Long> entry : getRanks().entrySet()) {
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(",");
        }
        
        String ranks;
        if (sb.length() > 0) {
            return sb.substring(0, sb.toString().length() - 1);
        } else {
            return "";
        }
    }
    
    public void loadRanks(String serialized) {
        if (serialized == null && serialized.equals("")) {
            return;
        }
        
        String[] rawRanks = serialized.split(",");
        if (rawRanks == null || rawRanks.length == 0) {
            return;
        }
        
        for (String rawRank : rawRanks) {
            String[] rankSplit = rawRank.split("=");
            if (rankSplit == null || rankSplit.length != 2) {
                continue;
            }
            
            Rank rank = Rank.valueOf(rankSplit[0]);
            long expire = Long.parseLong(rankSplit[1]);
            this.ranks.put(rank, expire);
        }
    }
    
    public String serializeTags() {
        return StringHelper.join(this.unlockedTags, ",");
    }
    
    public void loadTags(String serialized) {
        if (serialized == null && serialized.equals("")) {
            return;
        }
        
        String[] tagsSplit = serialized.split(",");
        if (tagsSplit == null || tagsSplit.length == 0) {
            return;
        }
        
        this.unlockedTags.addAll(Arrays.asList(tagsSplit));
    }
    
    public Map<Rank, Long> getRanks() {
        return new EnumMap<>(ranks);
    }
    
    public Map<String, Preference> getPreferences() {
        return new HashMap<>(preferences);
    }
    
    public Set<String> getUnlockedTags() {
        return new HashSet<>(unlockedTags);
    }
    
    public boolean isTagUnlocked(String tag) {
        return this.unlockedTags.contains(tag.toLowerCase());
    }
    
    public void unlockTag(String tag) {
        this.unlockedTags.add(tag.toLowerCase());
    }
    
    public void lockTag(String tag) {
        this.unlockedTags.remove(tag.toLowerCase());
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
}
