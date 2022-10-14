package com.thenexusreborn.api.stats;

import java.util.Set;

public class StatValue {
    protected StatType type;
    protected Object value;
    
    private StatValue() {}
    
    public StatValue(StatType type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    public StatType getType() {
        return type;
    }
    
    public void setType(StatType type) {
        this.type = type;
    }
    
    public Object get() {
        return value;
    }
    
    public void set(Object value) {
        this.value = value;
    }
    
    public int getAsInt() {
        return (int) value;
    }
    
    public double getAsDouble() {
        return (double) value;
    }
    
    public long getAsLong() {
        return (long) value;
    }
    
    public String getAsString() {
        return (String) value;
    }
    
    public boolean getAsBoolean() {
        return (boolean) value;
    }
    
    public Set<String> getAsStringSet() {
        return (Set<String>) value;
    }
}