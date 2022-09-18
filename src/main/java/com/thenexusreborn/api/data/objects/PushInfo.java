package com.thenexusreborn.api.data.objects;

import java.lang.reflect.Field;

class PushInfo {
    private final String sql;
    private final boolean generateKeys;
    private final Field primaryField;
    private final ObjectHandler objectHandler;
    
    public PushInfo(String sql, boolean generateKeys, Field primaryField, ObjectHandler handler) {
        this.sql = sql;
        this.generateKeys = generateKeys;
        this.primaryField = primaryField;
        this.objectHandler = handler;
    }
    
    public String getSql() {
        return sql;
    }
    
    public boolean isGenerateKeys() {
        return generateKeys;
    }
    
    public Field getPrimaryField() {
        return primaryField;
    }
    
    public ObjectHandler getObjectHandler() {
        return objectHandler;
    }
}
