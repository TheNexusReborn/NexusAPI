package com.thenexusreborn.api.frameworks.value;

/**
 * <p>
 * This class is a utility class to allow storing a variable type of values into the Storage API
 * This is mainly taken from the existing Stats System to make it so that it can be reused in multiple places and system
 * You must store this as a field
 * If you intend to store in the Storage API, you must specifiy a @ColumnInfo annotation with the ValueCodec class as the codec
 * If you intend to store an Enum, you must also provide enough space to store the fully qualified class name of the Enum type
 * You must also provide enough space in the column to store the Type and the actual value
 * </p>
 */
public class Value implements Cloneable {
    
    private Type type;
    private Object object;
    
    public Value(Type type, Object object) {
        this.type = type;
        this.object = object;
    }
    
    public Type getType() {
        return type;
    }
    
    public Object get() {
        return object;
    }
    
    public void set(Object object) {
        this.object = object;
    }
    
    public int getAsInt() {
        if (object instanceof Number n) {
            return n.intValue();
        } else if (object instanceof String str) {
            return Integer.parseInt(str);
        }
        return 0;
    }
    
    public double getAsDouble() {
        if (object instanceof Number n) {
            return n.doubleValue();
        } else if (object instanceof String str) {
            return Double.parseDouble(str);
        }
        return 0.0;
    }
    
    public long getAsLong() {
        if (object instanceof Number n) {
            return n.longValue();
        } else if (object instanceof String str) {
            return Long.parseLong(str);
            
        }
        return 0;
    }
    
    public String getAsString() {
        return (String) object;
    }
    
    public boolean getAsBoolean() {
        return (boolean) object;
    }
    
    public <T extends Enum<?>> T getAsEnum(Class<T> clazz) {
        return (T) object;
    }
    
    public enum Type {
        INTEGER, DOUBLE, STRING, LONG, BOOLEAN, ENUM
    }
    
    @Override
    public Value clone() {
        try {
            return (Value) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}