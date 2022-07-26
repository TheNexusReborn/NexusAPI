package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.player.Preference;
import com.thenexusreborn.api.player.Preference.Info;

public class PreferenceRegistry extends Registry<Preference.Info> {
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
