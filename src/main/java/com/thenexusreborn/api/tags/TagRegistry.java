package com.thenexusreborn.api.tags;

import me.firestar311.starlib.api.Registry;

public class TagRegistry extends Registry<String> {
    @Override
    public String get(String str) {
        for (String object : getRegisteredObjects().values()) {
            if (object.equalsIgnoreCase(str)) {
                return object;
            }
        }

        return null;
    }
}
