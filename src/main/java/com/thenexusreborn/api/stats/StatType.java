package com.thenexusreborn.api.stats;

import static com.thenexusreborn.api.stats.StatOperator.*;

public enum StatType {
    INTEGER(ADD, SUBTRACT, MUTLIPLY, DIVIDE, REPLACE, RESET, SET), 
    DOUBLE(ADD, SUBTRACT, MUTLIPLY, DIVIDE, REPLACE, RESET, SET),
    STRING(REPLACE, RESET, SET), 
    BOOLEAN(REPLACE, RESET, INVERT, SET), 
    LONG(ADD, SUBTRACT, MUTLIPLY, DIVIDE, REPLACE, RESET, SET);
    
    private final StatOperator[] allowedOperators;
    
    StatType(StatOperator... allowedOperators) {
        this.allowedOperators = allowedOperators;
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