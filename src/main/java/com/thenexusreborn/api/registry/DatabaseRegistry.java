package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.storage.objects.Database;

public class DatabaseRegistry extends Registry<Database> {
    
    @Override
    public Database get(String str) {
        for (Database object : getObjects()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
