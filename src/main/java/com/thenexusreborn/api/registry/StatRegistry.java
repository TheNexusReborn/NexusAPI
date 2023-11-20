package com.thenexusreborn.api.registry;

import com.stardevllc.starlib.registry.StringRegistry;
import com.thenexusreborn.api.stats.Stat;
import com.thenexusreborn.api.stats.Stat.Info;
import com.thenexusreborn.api.stats.StatHelper;
import com.thenexusreborn.api.stats.StatType;

public class StatRegistry extends StringRegistry<Info> {
    public void register(String name, StatType type, Object defaultValue) {
        register(StatHelper.formatStatName(name), new Stat.Info(StatHelper.formatStatName(name), type, defaultValue));
    }
    
    public void register(String name, String displayName, StatType type, Object defaultValue) {
        register(StatHelper.formatStatName(name), new Stat.Info(StatHelper.formatStatName(name), displayName, type, defaultValue));
    }
    
    @Override
    public Info get(String str) {
        for (Info object : getObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
