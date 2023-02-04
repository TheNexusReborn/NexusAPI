package com.thenexusreborn.api.nickname;

import com.starmediadev.starsql.annotations.column.ColumnIgnored;
import com.starmediadev.starsql.annotations.table.TableName;
import com.thenexusreborn.api.player.Rank;
import com.thenexusreborn.api.skins.Skin;

import java.util.UUID;

@TableName("nicknames")
public class Nickname {
    
    private long id;
    private UUID player;
    private SkinType skinType;
    private String skinIdentifier;
    private String displayName;
    private Rank displayRank;
    
    @ColumnIgnored
    private Skin skin;
    
    private Nickname() {}
    
    public Nickname(UUID player, SkinType skinType, String skinIdentifier, String displayName, Rank displayRank, Skin skin) {
        this.player = player;
        this.skinType = skinType;
        this.skinIdentifier = skinIdentifier;
        this.displayName = displayName;
        this.displayRank = displayRank;
        this.skin = skin;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public UUID getPlayer() {
        return player;
    }
    
    public void setPlayer(UUID player) {
        this.player = player;
    }
    
    public SkinType getSkinType() {
        return skinType;
    }
    
    public void setSkinType(SkinType skinType) {
        this.skinType = skinType;
    }
    
    public String getSkinIdentifier() {
        return skinIdentifier;
    }
    
    public void setSkinIdentifier(String skinIdentifier) {
        this.skinIdentifier = skinIdentifier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Rank getDisplayRank() {
        return displayRank;
    }
    
    public void setDisplayRank(Rank displayRank) {
        this.displayRank = displayRank;
    }
    
    public Skin getSkin() {
        return skin;
    }
    
    public void setSkin(Skin skin) {
        this.skin = skin;
    }
}