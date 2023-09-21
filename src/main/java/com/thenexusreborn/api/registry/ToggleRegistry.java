package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.player.Toggle.Info;
import me.firestar311.starlib.api.Registry;

public class ToggleRegistry extends Registry<Info> {
    public void register(String name, Rank rank, String displayName, String description, boolean defaultValue) {
        register(name, new Info(name, rank, displayName, description, defaultValue));
    }
    
    @Override
    public Info get(String str) {
        for (Info object : this.getRegisteredObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
