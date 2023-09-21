package com.thenexusreborn.api.registry;

import me.firestar311.starsql.api.objects.SQLDatabase;

import java.util.logging.Logger;

public class DatabaseRegistry extends me.firestar311.starsql.api.DatabaseRegistry {
    
    public DatabaseRegistry(Logger logger) {
        super(logger);
    }
    
    @Override
    public SQLDatabase get(String str) {
        for (SQLDatabase object : getRegisteredObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
