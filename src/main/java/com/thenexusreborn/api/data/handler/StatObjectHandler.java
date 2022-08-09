package com.thenexusreborn.api.data.handler;

import com.thenexusreborn.api.data.objects.*;
import com.thenexusreborn.api.stats.*;

import java.lang.reflect.Field;

public class StatObjectHandler extends ObjectHandler {
    
    private Field field;
    private StatType type;
    
    public StatObjectHandler(Object object, Database database, Table table) {
        super(object, database, table);
    }
    
    @Override
    public void afterLoad() {
        try {
            Object value = null;
            StatType type = null;
            Field field = null;
            if (object instanceof Stat) {
                Stat stat = (Stat) object;
                value = stat.getValue();
                type = stat.getType();
                field = object.getClass().getDeclaredField("value");
            } else if (object instanceof StatChange) {
                StatChange change = (StatChange) object;
                value = change.getValue();
                type = change.getType();
                field = object.getClass().getDeclaredField("value");
            } else if (object instanceof Stat.Info) {
                Stat.Info info = (Stat.Info) object;
                value = info.getDefaultValue();
                type = info.getType();
                field = object.getClass().getDeclaredField("defaultValue");
            }
        
            field.setAccessible(true);
        
            //This code will convert the stored field value to the serialized value
            field.set(object, StatHelper.parseValue(type, (String) value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void beforeSave() {
        try {
            Object value = null;
            
            if (object instanceof Stat) {
                Stat stat = (Stat) object; 
                value = stat.getValue();
                this.type = stat.getType();
                this.field = object.getClass().getDeclaredField("value");
            } else if (object instanceof StatChange) {
                StatChange change = (StatChange) object;
                value = change.getValue();
                this.type = change.getType();
                this.field = object.getClass().getDeclaredField("value");
            } else if (object instanceof Stat.Info) {
                Stat.Info info = (Stat.Info) object;
                value = info.getDefaultValue();
                this.type = info.getType();
                this.field = object.getClass().getDeclaredField("defaultValue");
            }
    
            field.setAccessible(true);
            
            //This code will convert the stored field value to the serialized value
            field.set(object, StatHelper.serializeStatValue(type, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void afterSave() {
        try {
            field.setAccessible(true);
            field.set(object, StatHelper.parseValue(type, (String) field.get(object)));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
