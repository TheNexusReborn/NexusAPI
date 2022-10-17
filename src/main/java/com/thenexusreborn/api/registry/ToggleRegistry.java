package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.player.Toggle;
import com.thenexusreborn.api.player.Toggle.Info;

public class ToggleRegistry extends Registry<Toggle.Info> {
    public void register(String name, String displayName, String description, boolean defaultValue) {
        register(new Info(name, displayName, description, defaultValue));
    }
    
    @Override
    public Info get(String str) {
        for (Info object : this.getObjects()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
