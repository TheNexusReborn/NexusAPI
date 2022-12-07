package com.thenexusreborn.api.frameworks.value;

/**
 * <p>
 *  This class is a utility class to allow storing a variable type of values into the Storage API
 *  This is mainly taken from the existing Stats System to make it so that it can be reused in multiple places and system
 *  You must store this as a field
 *  If you intend to store in the Storage API, you must specifiy a @ColumnInfo annotation with the ValueCodec class as the codec
 *  If you intend to store an Enum, you must also provide enough space to store the fully qualified class name of the Enum type
 *  You must also provide enough space in the column to store the Type and the actual value
 * </p>
 */
public class Value {
    
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
        return (int) object;
    }
    
    public double getAsDouble() {
        return (double) object;
    }
    
    public long getAsLong() {
        return (long) object;
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
}