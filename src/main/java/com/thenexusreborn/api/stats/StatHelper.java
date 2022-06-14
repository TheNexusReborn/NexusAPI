package com.thenexusreborn.api.stats;

public final class StatHelper {
    private StatHelper() {}
    
    public static String formatStatName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }
}