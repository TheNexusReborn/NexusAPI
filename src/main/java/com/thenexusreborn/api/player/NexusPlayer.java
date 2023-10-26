package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.storage.handler.PlayerObjectHandler;
import me.firestar311.starsql.api.annotations.column.ColumnIgnored;
import me.firestar311.starsql.api.annotations.table.TableHandler;
import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.UUID;

@TableName("players")
@TableHandler(PlayerObjectHandler.class)
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
    
    @ColumnIgnored
    protected Session session;
    
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
        getPlayer().sendMessage(message);
    }
    
    public String getTablistName() {
        if (getRank() == Rank.MEMBER) {
            return Rank.MEMBER.getColor() + getName();
        } else {
            return "&f" + getName();
        }
    }
    
    public PlayerProxy getPlayer() {
        if (this.playerProxy == null) {
            this.playerProxy = PlayerProxy.of(this.uniqueId);
        }
        return this.playerProxy;
    }
    
    public NexusPlayer getLastMessage() {
        return NexusAPI.getApi().getPlayerManager().getNexusPlayer(this.lastMessage);
    }
    
    public void setLastMessage(NexusPlayer nexusPlayer) {
        this.lastMessage = nexusPlayer.getUniqueId();
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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public boolean isOnline() {
        if (getPlayer() != null) {
            return getPlayer().isOnline();
        }
        return false;
    }
}
