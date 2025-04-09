package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nickexperience")
public class NickExperience extends PlayerExperience {
    
    private boolean persist;
    
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
    
    public boolean isPersist() {
        return persist;
    }
    
    public void setPersist(boolean persist) {
        this.persist = persist;
    }
    
    @Override
    public boolean addExperience(double xp) {
        super.addExperience(xp);
        return trueExperience.addExperience(xp);
    }
    
    protected NickExperience() {}
    
    public void setTrueExperience(PlayerExperience trueExperience) {
        this.trueExperience = trueExperience;
    }
}
