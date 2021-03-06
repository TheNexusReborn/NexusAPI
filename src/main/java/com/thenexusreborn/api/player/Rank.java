package com.thenexusreborn.api.player;

public enum Rank {
    NEXUS("&4", true, 10), 
    ADMIN("&c", true, 9),
    MOD("&5", true, 8), 
    HELPER("&2", true, 7), 
    VIP("&e", true, 6),
    ARCHITECT("&a", true, 5),
    MEDIA("&3", true, 4), 
    DIAMOND_PA("&b", true, "DIAMOND", 3.5, true),
    DIAMOND("&b", true, 3), 
    GOLD_PA("&6", true, "GOLD", 2.5, true),
    GOLD("&6", true, 2), 
    IRON_PA("&7", true, "IRON", 1.5),
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
}
