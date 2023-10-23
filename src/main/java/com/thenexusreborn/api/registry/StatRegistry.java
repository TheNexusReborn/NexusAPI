package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;
import me.firestar311.starlib.api.Registry;

public class StatRegistry extends Registry<Info> {
    public void register(String name, StatType type, Object defaultValue) {
        register(StatHelper.formatStatName(name), new Stat.Info(StatHelper.formatStatName(name), type, defaultValue));
    }
    
    public void register(String name, String displayName, StatType type, Object defaultValue) {
        register(StatHelper.formatStatName(name), new Stat.Info(StatHelper.formatStatName(name), displayName, type, defaultValue));
    }
    
    @Override
    public Info get(String str) {
        for (Info object : getRegisteredObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
