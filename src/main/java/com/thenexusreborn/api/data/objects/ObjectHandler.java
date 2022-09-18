package com.thenexusreborn.api.data.objects;

public abstract class ObjectHandler {
    
    protected final Object object;
    protected final Database database;
    protected final Table table;
    
    public ObjectHandler(Object object, Database database, Table table) {
        this.object = object;
        this.database = database;
        this.table = table;
    }
    
    
    public void afterLoad() {
        
    }
    
    public void beforeSave() {
        
    }
    
    public void afterSave() {
        
    }
}