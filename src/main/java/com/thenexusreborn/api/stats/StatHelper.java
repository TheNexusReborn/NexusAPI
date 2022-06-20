package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.NexusPlayer;

import java.util.*;

public final class StatHelper {
    private StatHelper() {}
    
    private static final Map<String, Object> defaultValues = new HashMap<>();
    
    public static void addDefaultValue(String statName, Object value) {
        defaultValues.put(formatStatName(statName), value);
    }
    
    public static Object getDefaultValue(String statName) {
        return defaultValues.get(formatStatName(statName));
    }
    
    public static String formatStatName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }
    
    public static final EnumSet<StatOperator> ARITHMETIC_OPERATORS = EnumSet.of(StatOperator.ADD, StatOperator.SUBTRACT, StatOperator.MULTIPLY, StatOperator.DIVIDE);
    
    public static void consolidateStats(NexusPlayer player) {
        for (StatChange statChange : new TreeSet<>(player.getStatChanges())) {
            Stat stat = player.getStats().get(statChange.getStatName());
            if (stat == null) {
                stat = new Stat(player.getUniqueId(), statChange.getStatName(), statChange.getType(), getDefaultValue(statChange.getStatName()), System.currentTimeMillis());
                player.addStat(stat);
            }
            
            if (!stat.getType().isAllowedOperator(statChange.getOperator())) {
                NexusAPI.getApi().getLogger().severe("Stat change for stat " + stat.getName() + " had the invalid operator " + statChange.getOperator().name() + " for type " + stat.getType().name());
                continue;
            }
            
            if (stat.getValue() == null) {
                NexusAPI.getApi().getLogger().warning("Stat " + stat.getName() + " failed to load a value or has no default value, using Java Defaults.");
                stat.setValue(stat.getType().getDefaultValue());
            }
            
            
        }
    }
}