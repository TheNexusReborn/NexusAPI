package com.thenexusreborn.api.scoreboard;

import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.scoreboard.wrapper.*;

import java.util.*;

public abstract class NexusScoreboard {
    
    public static final Map<Rank, String> BEGIN_CHARS = new HashMap<>();
    
    static {
        BEGIN_CHARS.put(Rank.DIAMOND_PA, "a");
        BEGIN_CHARS.put(Rank.DIAMOND, "b");
        BEGIN_CHARS.put(Rank.GOLD_PA, "c");
        BEGIN_CHARS.put(Rank.GOLD, "d");
        BEGIN_CHARS.put(Rank.IRON_PA, "e");
        BEGIN_CHARS.put(Rank.IRON, "f");
        BEGIN_CHARS.put(Rank.MEMBER, "g");
        BEGIN_CHARS.put(Rank.MEDIA, "h");
        BEGIN_CHARS.put(Rank.VIP, "i");
        BEGIN_CHARS.put(Rank.ARCHITECT, "j");
        BEGIN_CHARS.put(Rank.NEXUS, "k");
        BEGIN_CHARS.put(Rank.ADMIN, "l");
        BEGIN_CHARS.put(Rank.SR_MOD, "m");
        BEGIN_CHARS.put(Rank.MOD, "n");
        BEGIN_CHARS.put(Rank.HELPER, "o");
    }
    
    protected NexusPlayer player;
    protected ScoreboardView view;
    protected IScoreboard scoreboard;
    protected Map<UUID, ITeam> playerTeams = new HashMap<>();
    
    public NexusScoreboard(NexusPlayer player) {
        this.player = player;
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
    }
    
    public abstract void init();
    
    public static String getPlayerTeamName(NexusPlayer player) {
        String pName = player.getName();
        String name = BEGIN_CHARS.get(player.getRank()) + "_";
        if (pName.length() > 13) {
            name += pName.substring(0, 14);
        } else {
            name += pName;
        }
        return name;
    }
    
    public void createPlayerTeam(NexusPlayer nexusPlayer) {
        ITeam team = scoreboard.registerNewTeam(getPlayerTeamName(nexusPlayer));
        team.addEntry(nexusPlayer.getName());
        setTeamDisplayOptions(nexusPlayer, team);
        this.playerTeams.put(nexusPlayer.getUniqueId(), team);
    }
    
    public abstract void setTeamDisplayOptions(NexusPlayer nexusPlayer, ITeam team);
    
    public IScoreboard getScoreboard() {
        return scoreboard;
    }
    
    public void updatePlayerTeam(NexusPlayer nexusPlayer) {
        ITeam team = getExistingPlayerTeam(nexusPlayer);
        setTeamDisplayOptions(nexusPlayer, team);
    }

    public void refreshPlayerTeam(NexusPlayer nexusPlayer) {
        ITeam team = getExistingPlayerTeam(nexusPlayer);
        if (team != null) {
            team.unregister();
        }
        createPlayerTeam(nexusPlayer);
    }
    
    private ITeam getTeam(String name) {
        return scoreboard.getTeam(name);
    }

    private ITeam getExistingPlayerTeam(NexusPlayer nexusPlayer) {
        ITeam team = getTeam(getPlayerTeamName(nexusPlayer));
        if (team == null) {
            String playerName;
            if (nexusPlayer.getName().length() > 13) {
                playerName = nexusPlayer.getName().substring(0, 14);
            } else {
                playerName = nexusPlayer.getName();
            }
            for (ITeam scoreboardTeam : this.scoreboard.getTeams()) {
                if (scoreboardTeam.getName().contains(playerName)) {
                    team = scoreboardTeam;
                    break;
                }
            }
        }
        return team;
    }
    
    public Map<UUID, ITeam> getPlayerTeams() {
        return playerTeams;
    }
    
    public NexusPlayer getPlayer() {
        return player;
    }
}
