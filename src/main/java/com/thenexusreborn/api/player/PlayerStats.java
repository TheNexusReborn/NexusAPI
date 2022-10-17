package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.stats.*;

import java.util.*;

public class PlayerStats {
    protected final Map<String, Stat> stats = new HashMap<>();
    protected final Set<StatChange> statChanges = new TreeSet<>();
    
    protected UUID uniqueId;
    
    public PlayerStats(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public boolean has(String statName) {
        return this.stats.containsKey(statName);
    }
    
    public void add(Stat stat) {
        if (stat.getName() != null) {
            this.stats.put(stat.getName(), stat);
        }
    }
    
    public void addChange(StatChange statChange) {
        this.statChanges.add(statChange);
    }
    
    public Map<String, Stat> getStats() {
        return stats;
    }
    
    public StatValue getValue(String statName) {
        Stat stat = get(statName);
        if (stat == null) {
            Stat.Info info = StatHelper.getInfo(statName);
            stat = new Stat(info, this.uniqueId, System.currentTimeMillis());
            
            if (this.statChanges.size() == 0) {
                return new StatValue(info.getType(), info.getDefaultValue());
            }
        }
        
        stat = stat.clone();
    
        for (StatChange statChange : this.findAllChanges()) {
            if (statChange.getStatName().equalsIgnoreCase(stat.getName())) {
                StatHelper.changeStat(stat, statChange.getOperator(), statChange.getValue().get());
            }
        }
    
        return stat.getValue();
    }
    
    public Stat get(String name) {
        return this.stats.get(StatHelper.formatStatName(name));
    }
    
    public StatChange change(String statName, Object statValue, StatOperator operator) {
        Stat stat = get(statName);
        if (stat == null) {
            Stat.Info info = StatHelper.getInfo(statName);
            if (info == null) {
                NexusAPI.getApi().getLogger().warning("Could not find a stat with the name " + statName);
                return null;
            }
            stat = new Stat(info, this.uniqueId, info.getDefaultValue(), System.currentTimeMillis());
            this.add(stat);
        }
        StatChange statChange = new StatChange(stat.getInfo(), this.uniqueId, statValue, operator, System.currentTimeMillis());
        this.addChange(statChange);
        NexusAPI.getApi().getThreadFactory().runAsync(() -> {
            NexusAPI.getApi().getPrimaryDatabase().push(statChange); //Temporary for now until a change to the game stuff
        });
        return statChange;
    }
    
    public List<Stat> findAll() {
        return new ArrayList<>(this.stats.values());
    }
    
    public Set<StatChange> findAllChanges() {
        return new TreeSet<>(this.statChanges);
    }
    
    public void clearChanges() {
        this.statChanges.clear();
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
}
