package com.thenexusreborn.api.stats;

import me.firestar311.starlib.api.Value;
import me.firestar311.starlib.api.Value.Type;

import static com.thenexusreborn.api.stats.StatOperator.*;

public enum StatType {
    INTEGER(0, Type.INTEGER, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET, INVERT), 
    DOUBLE(0.0,  Type.DOUBLE, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET, INVERT),
    LONG(0, Type.LONG, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET, INVERT), 
    STRING("", Type.STRING, RESET, SET), 
    BOOLEAN(false, Type.BOOLEAN, RESET, INVERT, SET);
    
    private final Object defaultValue;
    private final Value.Type valueType;
    private final StatOperator[] allowedOperators;
    
    StatType(Object defaultValue, Value.Type valueType, StatOperator... allowedOperators) {
        this.defaultValue = defaultValue;
        this.valueType = valueType;
        this.allowedOperators = allowedOperators;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public StatOperator[] getAllowedOperators() {
        return allowedOperators;
    }
    
    public Type getValueType() {
        return valueType;
    }
    
    public boolean isAllowedOperator(StatOperator statOperator) {
        if (getAllowedOperators() != null) {
            for (StatOperator allowedOperator : allowedOperators) {
                if (allowedOperator == statOperator) {
                    return true;
                }
            }
        }
        
        return false;
    }
}