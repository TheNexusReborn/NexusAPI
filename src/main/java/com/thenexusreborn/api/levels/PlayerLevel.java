package com.thenexusreborn.api.levels;

import com.thenexusreborn.api.reward.Reward;

import java.util.ArrayList;
import java.util.List;

public class PlayerLevel {
    private final int number;
    private final int xpRequired;
    private final List<Reward> rewards = new ArrayList<>();

    public PlayerLevel(int number, int xpRequired) {
        this.number = number;
        this.xpRequired = xpRequired;
    }

    public int getNumber() {
        return number;
    }

    public int getXpRequired() {
        return xpRequired;
    }

    public void addReward(Reward reward) {
        this.rewards.add(reward);
    }

    public List<Reward> getRewards() {
        return new ArrayList<>(rewards);
    }
}
