package com.thenexusreborn.api.player;

import me.firestar311.starsql.api.annotations.column.PrimaryKey;
import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.UUID;

@TableName("balances")
public class PlayerBalance {
    @PrimaryKey private UUID uniqueId;
    private double nexites, credits;
    
    private PlayerBalance() {}

    public PlayerBalance(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public double getNexites() {
        return nexites;
    }

    public double getCredits() {
        return credits;
    }

    public void setNexites(double nexites) {
        this.nexites = nexites;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }
}