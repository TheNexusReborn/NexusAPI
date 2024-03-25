package com.thenexusreborn.api.player;

import com.stardevllc.starlib.Value;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.reward.Reward;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.stats.Stat;
import com.thenexusreborn.api.stats.StatChange;
import com.thenexusreborn.api.stats.StatOperator;
import com.thenexusreborn.api.storage.codec.RanksCodec;
import com.thenexusreborn.api.storage.handler.PlayerObjectHandler;
import me.firestar311.starsql.api.annotations.column.ColumnCodec;
import me.firestar311.starsql.api.annotations.column.ColumnIgnored;
import me.firestar311.starsql.api.annotations.column.ColumnType;
import me.firestar311.starsql.api.annotations.table.TableHandler;
import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@TableName("players")
@TableHandler(PlayerObjectHandler.class)
public class NexusPlayer {
    
    protected long id;
    protected UUID uniqueId;
    protected String name;
    
    @ColumnIgnored
    protected PlayerExperience experience;
    
    @ColumnIgnored
    protected PlayerTime playerTime;
    
    @ColumnIgnored
    protected PlayerBalance balance;
    
    @ColumnIgnored
    protected Set<IPEntry> ipHistory = new HashSet<>();
    @ColumnType("varchar(1000)")
    @ColumnCodec(RanksCodec.class)
    protected PlayerRanks ranks;
    @ColumnIgnored
    protected PlayerStats stats;
    @ColumnIgnored
    protected PlayerToggles toggles;
    @ColumnIgnored
    protected PlayerTags tags;
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
    protected Session session;
    
    protected NexusPlayer() {
        this(null);
    }
    
    public NexusPlayer(UUID uniqueId) {
        this(0, uniqueId, "");
    }
    
    public NexusPlayer(long id, UUID uniqueId, String name) {
        this.id = id;
        this.name = name;
        this.uniqueId = uniqueId;
        this.toggles = new PlayerToggles();
        this.stats = new PlayerStats(uniqueId);
        this.ranks = new PlayerRanks(uniqueId);
        this.tags = new PlayerTags(uniqueId);
        this.playerTime = new PlayerTime(uniqueId);
        this.experience = new PlayerExperience(uniqueId);
        this.balance = new PlayerBalance(uniqueId);
    }

    public PlayerBalance getBalance() {
        if (balance.getUniqueId() == null) {
            balance.setUniqueId(uniqueId);
        }
        return balance;
    }

    public PlayerExperience getExperience() {
        if (this.experience.getUniqueId() == null) {
            experience.setUniqueId(uniqueId);
        }
        return experience;
    }

    public PlayerTime getPlayerTime() {
        if (this.playerTime.getUniqueId() == null) {
            this.playerTime.setUniqueId(uniqueId);
        }
        return playerTime;
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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public long getFirstJoined() {
        return this.playerTime.getFirstJoined();
    }
    
    public void setFirstJoined(long firstJoined) {
        this.playerTime.setFirstJoined(firstJoined);
    }
    
    public long getLastLogin() {
        return this.playerTime.getLastLogin();
    }
    
    public void setLastLogin(long lastLogin) {
        this.playerTime.setLastLogin(lastLogin);
    }

    public String getDisplayName() {
        if (getRank() != Rank.MEMBER) {
            return getRank().getPrefix() + " &f" + getName();
        } else {
            return getRank().getPrefix() + getName();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NexusPlayer that = (NexusPlayer) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
    
    public long getLastLogout() {
        return this.playerTime.getLastLogout();
    }
    
    public void setLastLogout(long lastLogout) {
        this.playerTime.setLastLogout(lastLogout);
    }
    
    public PlayerStats getStats() {
        if (stats.getUniqueId() == null) {
            stats.setUniqueId(uniqueId);
        }
        return stats;
    }
    
    public Value getStatValue(String statName) {
        return getStats().getValue(statName);
    }
    
    public StatChange changeStat(String statName, Object value, StatOperator operator) {
        return getStats().change(statName, value, operator);
    }
    
    public void addStatChange(StatChange change) {
        getStats().addChange(change);
    }
    
    public void clearStatChanges() {
        getStats().clearChanges();
    }
    
    public void addStat(Stat stat) {
        getStats().add(stat);
    }

    public void addCredits(int credits) {
        this.balance.addCredits(credits);
    }
    
    public void addXp(double xp) {
        boolean leveledUp = this.experience.addExperience(xp);
        
        if (leveledUp) {
            if (this.playerProxy != null) {
                this.playerProxy.sendMessage("");
                this.playerProxy.sendMessage("&a&lLEVEL UP!");
                this.playerProxy.sendMessage("&e" + (this.experience.getLevel() - 1) + " &a-> &e" + this.experience.getLevel());
                this.playerProxy.sendMessage("");
                for (Reward reward : NexusAPI.getApi().getLevelManager().getLevel(this.experience.getLevel()).getRewards()) {
                    reward.applyReward(this);
                }
            }
        }
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<IPEntry> getIpHistory() {
        return ipHistory;
    }
    
    public PlayerRanks getRanks() {
        if (ranks.getUniqueId() == null) {
            ranks.setUniqueId(this.uniqueId);
        }
        return ranks;
    }
    
    public Rank getRank() {
        return getRanks().get();
    }
    
    public void addRank(Rank rank, long expire) {
        getRanks().add(rank, expire);
    }
    
    public void setRank(Rank rank, long expire) {
        getRanks().set(rank, expire);
    }
    
    public void removeRank(Rank rank) {
        getRanks().remove(rank);
    }
    
    public boolean hasRank(Rank rank) {
        return getRanks().contains(rank);
    }
    
    public PlayerToggles getToggles() {
        if (toggles.getUniqueId() == null) {
            toggles.setUniqueId(this.uniqueId);
        }
        return toggles;
    }
    
    public Toggle getToggle(String toggleName) {
        return getToggles().get(toggleName);
    }
    
    public boolean getToggleValue(String toggleName) {
        return getToggles().getValue(toggleName);
    }
    
    public void setToggleValue(String toggleName, boolean value) {
        getToggles().setValue(toggleName, value);
    }
    
    public String getColoredName() {
        return getRank().getColor() + getName();
    }
    
    public void removeCredits(int credits) {
        this.balance.addCredits(-credits);
    }
    
    public boolean isOnline() {
        PlayerProxy player = getPlayer();
        if (player != null) {
            return player.isOnline();
        }
        
        return false;
    }

    public PlayerTags getTags() {
        if (tags.getUuid() == null) {
            tags.setUuid(this.uniqueId);
        }
        return tags;
    }
    
    public void addToggle(Toggle toggle) {
        getToggles().add(toggle);
    }
}
