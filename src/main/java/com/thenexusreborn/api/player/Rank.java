package com.thenexusreborn.api.player;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public enum Rank {
    NEXUS("&4", true, 10), 
    ADMIN("&c", true, 10),
    HEAD_MOD("&5", true, "HEAD MOD", 9),
    SR_MOD("&5", true, "SR MOD", 8.5),
    MOD("&5", true, 8), 
    HELPER("&2", true, 7), 
    VIP("&e", true, 6),
    ARCHITECT("&a", true, 5),
    MEDIA("&3", true, 4), 
    PLATINUM("&b", true, 3.5, true),
    DIAMOND("&b", true, 3), 
    BRASS("&6", true, 2.5, true),
    GOLD("&6", true, 2), 
    INVAR("&7", true, 1.5),
    IRON("&7", true, 1), 
    MEMBER("&9", false, "", 1);
    
    private final String color, prefixOverride;
    private final boolean bold;
    private final double multiplier;
    private final boolean nexiteBoost;
    
    Rank(String color, boolean bold, double multiplier) {
        this(color, bold, null, multiplier);
    }
    
    Rank(String color, boolean bold, String prefixOverride, double multiplier) {
        this(color, bold, prefixOverride, multiplier, false);
    }
    
    Rank(String color, boolean bold, double multiplier, boolean nexiteBoost) {
        this(color, bold, null, multiplier, nexiteBoost);
    }
    
    Rank(String color, boolean bold, String prefixOverride, double multiplier, boolean nexiteBoost) {
        this.color = color;
        this.bold = bold;
        this.prefixOverride = prefixOverride;
        this.multiplier = multiplier;
        this.nexiteBoost = nexiteBoost;
    }
    
    public boolean isNexiteBoost() {
        return nexiteBoost;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getPrefixOverride() {
        return prefixOverride;
    }
    
    public String getPrefix() {
        String prefix = color;
        if (bold) {
            prefix += "&l";
        }
        if (prefixOverride != null) {
            prefix += prefixOverride;
        } else {
            prefix += name();
        }
        return prefix;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public static Rank parseRank(String str) {
        try {
            return valueOf(str);
        } catch (Exception e) {
            if (str.equalsIgnoreCase("iron_pa")) {
                return Rank.INVAR;
            } else if (str.equalsIgnoreCase("gold_pa")) {
                return Rank.BRASS;
            } else if (str.equalsIgnoreCase("diamond_pa")) {
                return Rank.PLATINUM;
            }
        }
        
        return null;
    }

    public static Rank getPrimaryRank(UUID uniqueId, Map<Rank, Long> ranks) {
        if (PlayerManager.NEXUS_TEAM.contains(uniqueId)) {
            return Rank.NEXUS;
        }

        for (Map.Entry<Rank, Long> entry : new EnumMap<>(ranks).entrySet()) {
            if (entry.getValue() == -1) {
                return entry.getKey();
            }

            if (System.currentTimeMillis() <= entry.getValue()) {
                return entry.getKey();
            }
        }

        return Rank.MEMBER;
    }
}
