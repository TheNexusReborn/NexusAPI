package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nickexperience")
public class NickExperience extends PlayerExperience {
    
    @ColumnIgnored
    private PlayerExperience mainExperience;
    
    public NickExperience(UUID uniqueId, int level, PlayerExperience mainExperience) {
        super(uniqueId);
        this.level = level;
        this.mainExperience = mainExperience;
    }
    
    @Override
    public boolean addExperience(double xp) {
        mainExperience.addExperience(xp);
        return super.addExperience(xp);
    }
    
    @Override
    public void setLevel(int level) {
        mainExperience.setLevel(level);
        super.setLevel(level);
    }
    
    @Override
    public void setLevelXp(double levelXp) {
        mainExperience.setLevelXp(levelXp);
        super.setLevelXp(levelXp);
    }
    
    protected NickExperience() {}
}
