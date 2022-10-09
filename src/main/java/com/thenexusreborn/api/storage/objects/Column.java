package com.thenexusreborn.api.storage.objects;

import com.thenexusreborn.api.storage.annotations.*;

import java.lang.reflect.Field;
import java.util.*;

public class Column implements Comparable<Column> {
    private final Class<?> modelClass;
    private final Field field;
    
    private String name, type;
    private boolean primaryKey, autoIncrement, notNull;
    private Class<? extends SqlCodec<?>> codec;
    
    public Column(Class<?> modelClass, Field field) {
        this.modelClass = modelClass;
        this.field = field;
        
        ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
        Primary primary = field.getAnnotation(Primary.class);
        if (columnInfo != null) {
            name = columnInfo.name();
            type = columnInfo.type();
            primaryKey = columnInfo.primaryKey();
            autoIncrement = columnInfo.autoIncrement();
            notNull = columnInfo.notNull();
            codec = (Class<? extends SqlCodec<?>>) columnInfo.codec();
        }
    
        if (primary != null) {
            if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                autoIncrement = true;
                primaryKey = true;
            } else {
                throw new IllegalArgumentException("Field " + field.getName() + " in class " + modelClass.getName() + " is marked as the primary field, but is not a long.");
            }
        }
    
        if (name == null || name.equals("")) {
            name = field.getName();
            //TODO make this to lower case after test
        }
    
        if (type == null || type.equals("")) {
            //TODO Have this detection using the codec system with default codecs
            Class<?> fieldType = field.getType();
            if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
                type = "int";
            } else if (String.class.equals(fieldType) || char.class.equals(fieldType) || Character.class.equals(fieldType)) {
                type = "varchar(1000)";
            } else if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
                type = "varchar(5)";
            } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
                type = "bigint";
            } else if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
                type = "double";
            } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
                type = "float";
            } else if (UUID.class.equals(fieldType)) {
                type = "varchar(36)";
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                type = "varchar(100)";
            }
        }
    }
    
    public Class<?> getModelClass() {
        return modelClass;
    }
    
    public Field getField() {
        return field;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public boolean isNotNull() {
        return notNull;
    }
    
    public Class<? extends SqlCodec<?>> getCodec() {
        return codec;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Column column = (Column) o;
        return Objects.equals(name, column.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public int compareTo(Column other) {
        if (this.primaryKey) {
            return 1;
        }
        
        if (other.primaryKey) {
            return -1;
        }
        
        return name.compareTo(other.name);
    }
}
