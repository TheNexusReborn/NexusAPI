package com.thenexusreborn.api.registry;

import com.starmediadev.starlib.util.Registry;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;

public class StatRegistry extends Registry<Info> {
    public void register(String name, StatType type, Object defaultValue) {
        register(new Stat.Info(StatHelper.formatStatName(name), type, defaultValue));
    }
    
    public void register(String name, String displayName, StatType type, Object defaultValue) {
        register(new Stat.Info(StatHelper.formatStatName(name), displayName, type, defaultValue));
    }
    
    @Override
    public Info get(String str) {
        for (Info object : getObjects()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
