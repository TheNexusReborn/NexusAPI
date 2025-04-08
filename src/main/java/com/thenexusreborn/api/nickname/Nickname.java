package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.column.PrimaryKey;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nicknames")
public class Nickname {
    @PrimaryKey
    private UUID uniqueId; //uuid of the player of the nickname
    private String name; //The name of the nickname
    private String trueName; //The true name of the player
    private String skin; //The identifier of the Skin. This must be compatible with the StarCore SkinManager
    private Rank rank; //The rank displayed
    
    @ColumnIgnored
    private PlayerExperience fakeExperience;
    
    @ColumnIgnored
    private PlayerBalance fakeBalance;
    
    @ColumnIgnored
    private PlayerTime fakeTime;
    
    private Nickname() {}
    
    public Nickname(UUID uniqueId, String name, String trueName, String skin, Rank rank) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.trueName = trueName;
        this.skin = skin;
        this.rank = rank;
        this.fakeExperience = new NickExperience(uniqueId);
        this.fakeBalance = new NickBalance(uniqueId);
        this.fakeTime = new NickTime(uniqueId);
    }
    
    public void copyFrom(Nickname nickname) {
        this.name = nickname.getName();
        this.skin = nickname.getSkin();
        this.rank = nickname.getRank();
        this.fakeExperience = nickname.getFakeExperience();
        this.fakeBalance = nickname.getFakeBalance();
        this.fakeTime = nickname.getFakeTime();
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getTrueName() {
        return trueName;
    }
    
    public String getSkin() {
        return skin;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public PlayerExperience getFakeExperience() {
        return fakeExperience;
    }
    
    public PlayerBalance getFakeBalance() {
        return fakeBalance;
    }
    
    public PlayerTime getFakeTime() {
        return fakeTime;
    }
    
    public void setFakeExperience(PlayerExperience fakeExperience) {
        this.fakeExperience = fakeExperience;
    }
    
    public void setFakeBalance(PlayerBalance fakeBalance) {
        this.fakeBalance = fakeBalance;
    }
    
    public void setFakeTime(PlayerTime fakeTime) {
        this.fakeTime = fakeTime;
    }
}