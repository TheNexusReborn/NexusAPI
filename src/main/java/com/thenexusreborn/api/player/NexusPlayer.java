package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.StatOperator;
import com.thenexusreborn.api.storage.annotations.*;
import com.thenexusreborn.api.storage.handler.PlayerObjectHandler;
import com.thenexusreborn.api.tags.Tag;

import java.util.UUID;

@TableInfo(value = "players", handler = PlayerObjectHandler.class)
public class NexusPlayer extends NexusProfile {
    @ColumnIgnored
    protected NexusScoreboard scoreboard;
    
    @ColumnIgnored
    protected UUID lastMessage;
    
    @ColumnIgnored
    protected IActionBar actionBar;
    
    @ColumnIgnored
    protected boolean spokenInChat;
    
    @ColumnIgnored
    protected PlayerProxy playerProxy;

    @ColumnIgnored
    protected int cps;
    
    private NexusPlayer() {
        this(null, "");
    }
    
    public NexusPlayer(UUID uniqueId) {
        this(uniqueId, "");
    }
    
    public NexusPlayer(UUID uniqueId, String name) {
        super(0, uniqueId, name);
    }
    
    public NexusScoreboard getScoreboard() {
        return scoreboard;
    }
    
    public void setScoreboard(NexusScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
    
    public void sendMessage(String message) {
        playerProxy.sendMessage(message);
    }
    
    public String getTablistName() {
        if (getRanks().get() == Rank.MEMBER) {
            return Rank.MEMBER.getColor() + getName();
        } else {
            return "&f" + getName();
        }
    }
    
    public PlayerProxy getPlayer() {
        return this.playerProxy;
    }
    
    public NexusPlayer getLastMessage() {
        return NexusAPI.getApi().getPlayerManager().getNexusPlayer(this.lastMessage);
    }
    
    public void setLastMessage(NexusPlayer nexusPlayer) {
        this.lastMessage = nexusPlayer.getUniqueId();
    }
    
    public Tag getTag() {
        String tag = this.getStats().getValue("tag").getAsString();
        if (tag == null || tag.equalsIgnoreCase("null")) {
            return null;
        }
        return new Tag(tag);
    }
    
    public void setTag(Tag tag) {
        if (tag != null) {
            getStats().change("tag", tag.getName(), StatOperator.SET);
        } else {
            getStats().change("tag", "null", StatOperator.SET);
        }
    }
    
    public void setLastMessage(UUID lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public IActionBar getActionBar() {
        return actionBar;
    }
    
    public void setActionBar(IActionBar actionBar) {
        this.actionBar = actionBar;
    }
    
    public void setSpokenInChat(boolean spokenInChat) {
        this.spokenInChat = spokenInChat;
    }
    
    public boolean hasSpokenInChat() {
        return this.spokenInChat;
    }
    
    public void setPlayerProxy(PlayerProxy playerProxy) {
        this.playerProxy = playerProxy;
    }

    public int getCPS() {
        return cps;
    }

    public void setCPS(int cps) {
        this.cps = cps;
    }

    public void incrementCPS() {
        this.cps++;
    }

    public void resetCPS() {
        this.cps = 0;
    }
    
    @Override
    public boolean isOnline() {
        return playerProxy.isOnline();
    }
}
