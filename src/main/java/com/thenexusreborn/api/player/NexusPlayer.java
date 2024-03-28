package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.reward.Reward;
import com.thenexusreborn.api.scoreboard.NexusScoreboard;
import com.thenexusreborn.api.sql.annotations.column.ColumnCodec;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.column.ColumnType;
import com.thenexusreborn.api.sql.annotations.table.TableHandler;
import com.thenexusreborn.api.sql.annotations.table.TableName;
import com.thenexusreborn.api.sql.objects.codecs.RanksCodec;
import com.thenexusreborn.api.sql.objects.objecthandler.PlayerObjectHandler;
import com.thenexusreborn.api.tags.Tag;

import java.util.*;

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
    protected PlayerToggles toggles;
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
    
    @ColumnIgnored
    private Map<String, Tag> tags = new HashMap<>();
    
    private String activeTag;
    
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
        this.ranks = new PlayerRanks(uniqueId);
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

    public void addToggle(Toggle toggle) {
        getToggles().add(toggle);
    }

    public Tag getActiveTag() {
        return this.tags.get(activeTag);
    }

    public void setActiveTag(String active) {
        if (active == null || active.equalsIgnoreCase("null")) {
            this.activeTag = null;
        }
        if (this.tags.containsKey(active)) {
            this.activeTag = active;
        }
    }

    public boolean hasActiveTag() {
        return activeTag != null && !activeTag.isEmpty() && !activeTag.equals("null");
    }

    public void addTag(Tag tag) {
        this.tags.put(tag.getName(), tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public void addAllTags(List<Tag> tags) {
        tags.forEach(this::addTag);
    }

    public boolean isTagUnlocked(String tag) {
        return this.tags.containsKey(tag);
    }

    public Set<String> getTags() {
        return new HashSet<>(this.tags.keySet());
    }
}
