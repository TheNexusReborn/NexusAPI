package com.thenexusreborn.api.stats;

import static com.thenexusreborn.api.stats.StatOperator.*;

public enum StatType {
    INTEGER(0, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET), 
    DOUBLE(0.0, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET),
    STRING("", RESET, SET), 
    BOOLEAN(false, RESET, INVERT, SET), 
    LONG(0, ADD, SUBTRACT, MULTIPLY, DIVIDE, RESET, SET);
    
    private final Object defaultValue;
    private final StatOperator[] allowedOperators;
    
    StatType(Object defaultValue, StatOperator... allowedOperators) {
        this.defaultValue = defaultValue;
        this.allowedOperators = allowedOperators;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public StatOperator[] getAllowedOperators() {
        return allowedOperators;
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