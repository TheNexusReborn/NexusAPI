package com.thenexusreborn.api.tags;

import com.stardevllc.starlib.registry.StringRegistry;

public class TagRegistry extends StringRegistry<String> {
    @Override
    public String get(String str) {
        for (String object : getObjects().values()) {
            if (object.equalsIgnoreCase(str)) {
                return object;
            }
        }

        return null;
    }
}
