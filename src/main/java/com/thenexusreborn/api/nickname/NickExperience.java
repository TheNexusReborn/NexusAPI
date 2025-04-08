package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nickexperience")
public class NickExperience extends PlayerExperience {
    public NickExperience(UUID uniqueId, int level) {
        super(uniqueId);
        this.level = level;
    }
    
    protected NickExperience() {}
}
