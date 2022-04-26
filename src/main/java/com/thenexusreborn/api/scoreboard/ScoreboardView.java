package com.thenexusreborn.api.scoreboard;

import com.thenexusreborn.api.scoreboard.wrapper.*;

import java.util.List;

public abstract class ScoreboardView {
    
    protected NexusScoreboard scoreboard;
    protected String name, displayName;
    
    public ScoreboardView(NexusScoreboard scoreboard, String name, String displayName) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = displayName;
    }
    
    protected IObjective objective;
    
    public abstract void registerTeams();
    public abstract void update();
    public abstract void registerObjective();
    public abstract List<String> getTeams();
    
    protected void addEntry(IObjective objective, ITeam team, String text, int score) {
        team.addEntry(text);
        objective.getScore(text).setScore(score);
    }

    public IObjective getObjective() {
        return objective;
    }
}
