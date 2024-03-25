package com.thenexusreborn.api.experience;

import me.firestar311.starsql.api.annotations.column.PrimaryKey;
import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.UUID;

@TableName("experience")
public class PlayerExperience {
    @PrimaryKey private UUID uniqueId;
    private int level;
    private double levelXp;

    private PlayerExperience() {}

    public PlayerExperience(UUID uniqueId, int level, double levelXp) {
        this.uniqueId = uniqueId;
        this.level = level;
        this.levelXp = levelXp;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLevelXp(double levelXp) {
        this.levelXp = levelXp;
    }

    public int getLevel() {
        return level;
    }

    public double getLevelXp() {
        return levelXp;
    }
}