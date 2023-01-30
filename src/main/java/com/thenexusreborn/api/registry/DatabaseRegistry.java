package com.thenexusreborn.api.registry;

import com.starmediadev.starsql.objects.Database;

import java.util.logging.Logger;

public class DatabaseRegistry extends com.starmediadev.starsql.DatabaseRegistry {
    
    public DatabaseRegistry(Logger logger) {
        super(logger);
    }
    
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
