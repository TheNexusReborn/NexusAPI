package com.thenexusreborn.api.tags;

import com.thenexusreborn.api.registry.Registry;

public class TagRegistry extends Registry<String> {
    @Override
    public String get(String str) {
        for (String object : getObjects()) {
            if (object.equalsIgnoreCase(str)) {
                return object;
            }
        }

        return null;
    }
}
