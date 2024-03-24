package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.registry.StatRegistry;
import com.thenexusreborn.api.stats.Stat.Info;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public final class StatHelper {
    private StatHelper() {
    }
    
    private static final StatRegistry registry = new StatRegistry();
    
    public static StatRegistry getRegistry() {
        return registry;
    }
    
    public static void addStatInfo(Stat.Info info) {
        registry.register(info.getName(), info);
    }
    
    public static Stat.Info getInfo(String name) {
        String statName = formatStatName(name);
        for (Info info : registry.getObjects().values()) {
            if (info.getName().equals(statName)) {
                return info;
            }
        }
        return null;
    }
    
    public static String formatStatName(String name) {
        if (name != null) {
            return name.toLowerCase().replace(" ", "_");
        }
        return "";
    }
    
    public static double calculate(StatOperator operator, Number number1, Number number2) {
        double value1 = number1.doubleValue();
        double value2 = number2.doubleValue();
        if (operator == StatOperator.INVERT) {
            return value1 * -1;
        } else if (operator == StatOperator.ADD) {
            return value1 + value2;
        } else if (operator == StatOperator.SUBTRACT) {
            return value1 - value2;
        } else if (operator == StatOperator.MULTIPLY) {
            return value1 * value2;
        } else if (operator == StatOperator.DIVIDE) {
            if (value2 == 0) {
                return 0;
            }
            return value1 / value2;
        }
        return 0;
    }
    
    public static void changeStat(Stat stat, StatChange change) {
        Object newValue = null;

        if (change.getOperator() == StatOperator.SET) {
            newValue = change.getValue();
        } else if (change.getOperator() == StatOperator.RESET) {
            newValue = getInfo(stat.getName()).getDefaultValue().get();
        } else {
            Object oldValue = stat.getValue().get();
            if (stat.getType() == StatType.BOOLEAN) {
                newValue = !((boolean) oldValue);
            } else if (Stream.of(StatType.INTEGER, StatType.DOUBLE, StatType.LONG).anyMatch(statType -> stat.getType() == statType)) {
                double calculated = calculate(change.getOperator(), (Number) oldValue, (Number) change.getValue().get());
                if (stat.getType() == StatType.INTEGER) {
                    newValue = (int) calculated;
                } else if (stat.getType() == StatType.DOUBLE) {
                    newValue = calculated;
                } else if (stat.getType() == StatType.LONG) {
                    newValue = (long) calculated;
                } else {
                    NexusAPI.getApi().getLogger().warning("Unhandled number type for stat " + stat.getName());
                    return;
                }
            }
        }
        stat.setValue(newValue);
    }
    
    public static StatChange changeStat(Stat stat, StatOperator operator, Object value) {
        StatChange statChange = new StatChange(stat.getInfo(), stat.getUuid(), value, operator, System.currentTimeMillis());
        changeStat(stat, statChange);
        return statChange;
    }
    
    public static void consolidateStats(NexusPlayer player) {
        try {
            List<Stat> rawStats = NexusAPI.getApi().getPrimaryDatabase().get(Stat.class, "uuid", player.getUniqueId().toString());
            Map<String, Stat> stats = new HashMap<>();
            for (Stat rawStat : rawStats) {
                stats.put(rawStat.getName(), rawStat);
            }
            
            Set<StatChange> statChanges = new TreeSet<>(NexusAPI.getApi().getPrimaryDatabase().get(StatChange.class, "uuid", player.toString()));
            for (StatChange statChange : statChanges) {
                Info info = getInfo(statChange.getStatName());
                Stat stat = stats.getOrDefault(statChange.getStatName(), new Stat(info, player.getUniqueId(), info.getDefaultValue(), System.currentTimeMillis()));
                if (!stats.containsKey(info.getName())) {
                    stats.put(stat.getName(), stat);
                }
                
                if (!stat.getType().isAllowedOperator(statChange.getOperator())) {
                    NexusAPI.getApi().getLogger().severe("Stat change for stat " + stat.getName() + " had the invalid operator " + statChange.getOperator().name() + " for type " + stat.getType().name());
                    continue;
                }
                
                if (stat.getValue() == null) {
                    NexusAPI.getApi().getLogger().warning("Stat " + stat.getName() + " failed to load a value or has no default value, using Java Defaults.");
                    stat.setValue(stat.getType().getDefaultValue());
                }
                
                changeStat(stat, statChange);
                if (statChange.getId() > 0) {
                    NexusAPI.getApi().getPrimaryDatabase().delete(StatChange.class, statChange.getId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}