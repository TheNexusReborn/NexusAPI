package com.thenexusreborn.api.stats;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.registry.StatRegistry;
import com.thenexusreborn.api.stats.Stat.Info;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class StatHelper {
    private StatHelper() {}
    
    private static final StatRegistry registry = new StatRegistry();
    
    public static StatRegistry getRegistry() {
        return registry;
    }
    
    public static void addStatInfo(Stat.Info info) {
        registry.register(info);
    }
    
    public static Stat.Info getInfo(String name) {
        String statName = formatStatName(name);
        for (Info info : registry.getObjects()) {
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
    
    public static StatChange changeStat(Stat stat, StatOperator operator, Object value) {
        Object newValue = null;
    
        if (operator == StatOperator.SET) {
            newValue = value;
        } else if (operator == StatOperator.RESET) {
            newValue = getInfo(stat.getName()).getDefaultValue();
        } else {
            Object oldValue = stat.getValue().get();
            if (stat.getType() == StatType.BOOLEAN) {
                newValue = !((boolean) oldValue);
            } else if (stat.getType() == StatType.INTEGER || stat.getType() == StatType.DOUBLE || stat.getType() == StatType.LONG) {
                double calculated = calculate(operator, (Number) oldValue, (Number) value);
                if (stat.getType() == StatType.INTEGER) {
                    newValue = (int) calculated;
                } else if (stat.getType() == StatType.DOUBLE) {
                    newValue = calculated;
                } else if (stat.getType() == StatType.LONG) {
                    newValue = (long) calculated;
                } else {
                    NexusAPI.getApi().getLogger().warning("Unhandled number type for stat " + stat.getName());
                    return null;
                }
            }
        }
        stat.setValue(newValue);
        return new StatChange(stat.getInfo(), stat.getUuid(), newValue, operator, System.currentTimeMillis());
    }
    
    public static void consolidateStats(NexusPlayer player) {
        try {
            List<StatChange> statChanges = NexusAPI.getApi().getPrimaryDatabase().get(StatChange.class, "uuid", player.getUniqueId().toString());
            statChanges.addAll(player.getStats().findAllChanges());
            for (StatChange statChange : new TreeSet<>(statChanges)) {
                if (statChange.getId() == 0) {
                    NexusAPI.getApi().getLogger().info("Stat Change for stat " + statChange.getStatName() + " had an ID of 0");
                    continue;
                }
                Stat stat = player.getStats().get(statChange.getStatName());
                if (stat == null) {
                    Info info = getInfo(statChange.getStatName());
                    stat = new Stat(info, player.getUniqueId(), info.getDefaultValue(), System.currentTimeMillis());
                    player.getStats().add(stat);
                }
        
                if (!stat.getType().isAllowedOperator(statChange.getOperator())) {
                    NexusAPI.getApi().getLogger().severe("Stat change for stat " + stat.getName() + " had the invalid operator " + statChange.getOperator().name() + " for type " + stat.getType().name());
                    continue;
                }
        
                if (stat.getValue() == null) {
                    NexusAPI.getApi().getLogger().warning("Stat " + stat.getName() + " failed to load a value or has no default value, using Java Defaults.");
                    stat.setValue(stat.getType().getDefaultValue());
                }
        
                changeStat(stat, statChange.getOperator(), statChange.getValue().get());
                if (statChange.getId() > 0) {
                    NexusAPI.getApi().getPrimaryDatabase().delete(StatChange.class, statChange.getId());
                }
            }
            player.getStats().clearChanges();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static String serializeStatValue(StatType type, Object value) {
        if (type == null) {
            return "null";
        }
        
        if (value == null) {
            return "null";
        }
        
        if (type == StatType.STRING_SET) {
            if (value instanceof String) {
                return (String) value;
            }
            StringBuilder sb = new StringBuilder();
            Iterator<String> iterator = ((Set<String>) value).iterator();
            while (iterator.hasNext()) {
                String e = iterator.next();
                sb.append(e);
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            
            return sb.toString();
        }
        
        return value.toString();
    }
    
    public static Object parseValue(StatType type, String raw) {
        if (type == null) {
            NexusAPI.logMessage(Level.SEVERE, "Could not parse a value for a stat because the provided type is null");
            return null;
        }
        
        if (raw == null || raw.equals("") || raw.equalsIgnoreCase("null")) {
            return type.getDefaultValue();
        }
        
        if (raw.startsWith(type.name() + ":")) {
            raw = raw.split(":")[1];
        }
        
        try {
            switch (type) {
                case INTEGER -> {
                    return Integer.parseInt(raw);
                }
                case DOUBLE -> {
                    return Double.parseDouble(raw);
                }
                case LONG -> {
                    return Long.parseLong(raw);
                }
                case BOOLEAN -> {
                    return Boolean.parseBoolean(raw);
                }
                case STRING -> {
                    return raw;
                }
                case STRING_SET -> {
                    Set<String> value = (Set<String>) type.getDefaultValue();
                    String[] split = raw.split(",");
                    if (split != null) {
                        value.addAll(Arrays.asList(split));
                    }
                    return value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}