package com.thenexusreborn.api.scoreboard;

import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.scoreboard.wrapper.*;

import java.util.*;

public abstract class NexusScoreboard {
    
    protected NexusPlayer player;
    protected ScoreboardView view;
    protected IScoreboard scoreboard;
    protected TablistHandler tablistHandler;
    
    public NexusScoreboard(NexusPlayer player) {
        this.player = player;
    }
    
    public Set<ITeam> getTeams() {
        return this.scoreboard.getTeams();
    }
    
    public TablistHandler getTablistHandler() {
        return tablistHandler;
    }
    
    public void setTablistHandler(TablistHandler tablistHandler) {
        if (this.tablistHandler != null) {
            this.tablistHandler.unregister();
        }
        this.tablistHandler = tablistHandler;
    }
    
    public void setView(ScoreboardView view) {
        if (this.view != null) {
            this.view.getObjective().unregister();
            for (String team : this.view.getTeams()) {
                try {
                    ITeam registeredTeam = this.scoreboard.getTeam(team);
                    for (String entry : registeredTeam.getEntries()) {
                        scoreboard.resetScores(entry);
                    }
                    registeredTeam.unregister();
                } catch (IllegalArgumentException e) {}
            }
        }

        if (view != null) {
            view.registerObjective();
            view.registerTeams();
        }
        this.view = view;
    }
    
    public void update() {
        if (view != null) {
            view.update();
        }
        
        if (tablistHandler != null) {
            tablistHandler.update();
        }
    }
    
    public abstract void init();
    
    public IScoreboard getScoreboard() {
        return scoreboard;
    }
    
    public ITeam getTeam(String name) {
        return scoreboard.getTeam(name);
    }

    public abstract void apply();
    
    public ITeam registerNewTeam(String name) {
        return this.scoreboard.registerNewTeam(name);
    }
    
    public NexusPlayer getPlayer() {
        return player;
    }
}
