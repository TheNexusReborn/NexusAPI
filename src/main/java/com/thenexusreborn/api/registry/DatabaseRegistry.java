package com.thenexusreborn.api.registry;

import com.starmediadev.starsql.objects.Database;

public class DatabaseRegistry extends com.starmediadev.starsql.DatabaseRegistry<Database> {
    
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
