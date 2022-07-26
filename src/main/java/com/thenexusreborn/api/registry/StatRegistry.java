package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.stats.*;

public class StatRegistry extends Registry<Stat.Info> {
    public void register(String name, StatType type, Object defaultValue) {
        register(new Stat.Info(StatHelper.formatStatName(name), type, defaultValue));
    }
}
