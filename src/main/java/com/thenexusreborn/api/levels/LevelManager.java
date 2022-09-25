package com.thenexusreborn.api.levels;

import java.util.HashMap;
import java.util.Map;

public class LevelManager {
    //TODO Make this instance instead of static and add a proper level class
    public static final Map<Integer, Integer> levels = new HashMap<>();
    
    static {
        LevelManager.levels.put(0, 0);
        int xp = 5000;
        for (int i = 1; i <= 100; i++) {
            if (i <= 5) {
                int xpToLevel = i * 1000;
                int totalXp;
                if (i > 1) {
                    totalXp = LevelManager.levels.get(i - 1) + xpToLevel;
                } else {
                    totalXp = xpToLevel;
                }
                LevelManager.levels.put(i, totalXp);
            } else {
                int totalXp = LevelManager.levels.get(i - 1) + 5000;
                LevelManager.levels.put(i, totalXp);
            }
        }
    }

    private Map<Integer, PlayerLevel> playerLevels = new HashMap<>();

    public void init() {
        addLevel(new PlayerLevel(0, 0));
        for (int i = 1; i <= 100; i++) {
            PlayerLevel playerLevel;
            if (i <= 5) {
                playerLevel = new PlayerLevel(i, i * 1000);
            } else {
                playerLevel = new PlayerLevel(i, 5000);
            }
            playerLevel.addReward(new CreditReward(i * 100));
            addLevel(playerLevel);
        }
    }

    public void addLevel(PlayerLevel playerLevel) {
        this.playerLevels.put(playerLevel.getNumber(), playerLevel);
    }

    public Map<Integer, PlayerLevel> getPlayerLevels() {
        return playerLevels;
    }
}
