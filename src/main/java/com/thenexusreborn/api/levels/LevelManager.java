package com.thenexusreborn.api.levels;

import java.util.HashMap;
import java.util.Map;

public class LevelManager {
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
    
    public PlayerLevel getLevel(int level) {
        return this.playerLevels.get(level);
    }

    public void addLevel(PlayerLevel playerLevel) {
        this.playerLevels.put(playerLevel.getNumber(), playerLevel);
    }

    public Map<Integer, PlayerLevel> getPlayerLevels() {
        return new HashMap<>(playerLevels);
    }
}
