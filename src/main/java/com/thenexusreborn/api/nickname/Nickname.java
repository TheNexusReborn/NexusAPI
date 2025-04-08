package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.player.Rank;
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
    
    private Nickname() {}
    
    public Nickname(UUID uniqueId, String name, String trueName, String skin, Rank rank, PlayerExperience fakeExperience) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.trueName = trueName;
        this.skin = skin;
        this.rank = rank;
        this.fakeExperience = fakeExperience;
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
    
    public void setFakeExperience(PlayerExperience fakeExperience) {
        this.fakeExperience = fakeExperience;
    }
}