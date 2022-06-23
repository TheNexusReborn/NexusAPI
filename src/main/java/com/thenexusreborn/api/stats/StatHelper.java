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
    
    public static void changeStat(Stat stat, StatOperator operator, Object value) {
        Object newValue = null;
        if (operator == StatOperator.SET) {
            newValue = value;
        } else if (operator == StatOperator.RESET) {
            newValue = StatHelper.getDefaultValue(stat.getName());
        } else {
            Object oldValue = stat.getValue();
            if (stat.getType() == StatType.BOOLEAN) {
                newValue = !((boolean) oldValue);
            } else if (stat.getType() == StatType.INTEGER || stat.getType() == StatType.DOUBLE || stat.getType() == StatType.LONG) {
                double calculated = calculate(operator, (Number) stat.getValue(), (Number) value);
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
            
            changeStat(stat, statChange.getOperator(), statChange.getValue());
        }
    }
}