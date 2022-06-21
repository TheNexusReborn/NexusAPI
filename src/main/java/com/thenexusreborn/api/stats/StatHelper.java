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
    
    @SuppressWarnings("DuplicatedCode")
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
            
            StatOperator operator = statChange.getOperator();
            Object newValue = null;
            if (operator == StatOperator.SET) {
                newValue = statChange.getValue();
            } else if (operator == StatOperator.RESET) {
                newValue = StatHelper.getDefaultValue(stat.getName());
            } else {
                Object oldValue = stat.getValue();
                if (stat.getType() == StatType.BOOLEAN) {
                    newValue = !((boolean) oldValue);
                } else if (stat.getType() == StatType.INTEGER || stat.getType() == StatType.DOUBLE || stat.getType() == StatType.LONG) {
                    Number number = (Number) stat.getValue();
                    if (number instanceof Integer) {
                        Integer integerValue = (Integer) number;
                        Integer changedValue = (Integer) statChange.getValue();
                        if (operator == StatOperator.INVERT) {
                            newValue = integerValue * -1;
                        } else if (operator == StatOperator.ADD) {
                            newValue = integerValue + changedValue;
                        } else if (operator == StatOperator.SUBTRACT) {
                            newValue = integerValue - changedValue;
                        } else if (operator == StatOperator.MULTIPLY) {
                            newValue = integerValue * changedValue;
                        } else if (operator == StatOperator.DIVIDE) {
                            newValue = integerValue / changedValue;
                        }
                    } else if (number instanceof Double) {
                        Double doubleValue = (Double) number;
                        Double changedValue = (Double) statChange.getValue();
                        if (operator == StatOperator.INVERT) {
                            newValue = doubleValue * -1;
                        } else if (operator == StatOperator.ADD) {
                            newValue = doubleValue + changedValue;
                        } else if (operator == StatOperator.SUBTRACT) {
                            newValue = doubleValue - changedValue;
                        } else if (operator == StatOperator.MULTIPLY) {
                            newValue = doubleValue * changedValue;
                        } else if (operator == StatOperator.DIVIDE) {
                            newValue = doubleValue / changedValue;
                        }
                    } else if (number instanceof Long) {
                        Long longValue = (Long) number;
                        Long changedValue = (Long) statChange.getValue();
                        if (operator == StatOperator.INVERT) {
                            newValue = longValue * -1;
                        } else if (operator == StatOperator.ADD) {
                            newValue = longValue + changedValue;
                        } else if (operator == StatOperator.SUBTRACT) {
                            newValue = longValue - changedValue;
                        } else if (operator == StatOperator.MULTIPLY) {
                            newValue = longValue * changedValue;
                        } else if (operator == StatOperator.DIVIDE) {
                            newValue = longValue / changedValue;
                        }
                    } else {
                        NexusAPI.getApi().getLogger().warning("Unhandled number type for stat " + stat.getName());
                        continue;
                    }
                }
            }
            stat.setValue(newValue);
        }
    }
}