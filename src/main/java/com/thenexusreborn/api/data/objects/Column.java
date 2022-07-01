package com.thenexusreborn.api.data.objects;

import java.util.Objects;

public class Column implements Comparable<Column> {
    private final String name, type;
    private final boolean primaryKey, autoIncrement, notNull;
    private Class<? extends SqlCodec<?>> codec;
    
    public Column(String name, String type, boolean primaryKey, boolean autoIncrement, boolean notNull, Class<? extends SqlCodec<?>> codec) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.notNull = notNull;
        this.codec = codec;
    }
    
    public Column(String name, String type) {
        this(name, type, false, false, false, null);
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
        
        if (this.autoIncrement) {
            return 1;
        }
        
        if (other.autoIncrement) {
            return -1;
        }
        
        return name.compareTo(other.name);
    }
}
