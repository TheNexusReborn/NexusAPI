package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nickexperience")
public class NickExperience extends PlayerExperience {
    
    @ColumnIgnored
    private PlayerExperience trueExperience;
    
    public NickExperience(UUID uniqueId) {
        super(uniqueId);
    }
    
    public NickExperience(UUID uniqueId, int level, PlayerExperience trueExperience) {
        super(uniqueId);
        this.level = level;
        this.trueExperience = trueExperience;
    }
    
    @Override
    public boolean addExperience(double xp) {
        super.addExperience(xp);
        return trueExperience.addExperience(xp);
    }
    
    @Override
    public void setLevel(int level) {
        trueExperience.setLevel(level);
        super.setLevel(level);
    }
    
    @Override
    public void setLevelXp(double levelXp) {
        trueExperience.setLevelXp(levelXp);
        super.setLevelXp(levelXp);
    }
    
    protected NickExperience() {}
    
    public void setTrueExperience(PlayerExperience trueExperience) {
        this.trueExperience = trueExperience;
    }
}
