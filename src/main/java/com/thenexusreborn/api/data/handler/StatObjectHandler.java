package com.thenexusreborn.api.data.handler;

import com.thenexusreborn.api.data.objects.*;
import com.thenexusreborn.api.stats.*;

import java.lang.reflect.Field;

public class StatObjectHandler extends ObjectHandler {
    
    private Object value;
    private Field field;
    
    public StatObjectHandler(Object object, Database database, Table table) {
        super(object, database, table);
    }
    
    @Override
    public void beforeSave() {
        try {
            value = null;
            Field valueField = null;
            StatType type = null;
            
            if (object instanceof Stat) {
                Stat stat = (Stat) object; 
                value = stat.getValue();
                type = stat.getType();
                valueField = object.getClass().getField("value");
            } else if (object instanceof StatChange) {
                StatChange change = (StatChange) object;
                value = change.getValue();
                type = change.getType();
                valueField = object.getClass().getField("value");
            } else if (object instanceof Stat.Info) {
                Stat.Info info = (Stat.Info) object;
                value = info.getDefaultValue();
                type = info.getType();
                valueField = object.getClass().getField("defaultValue");
            }
            
            valueField.setAccessible(true);
            
            //This code will convert the stored field value to the serialized value
            valueField.set(object, StatHelper.serializeStatValue(type, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void afterSave() {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
