package com.thenexusreborn.api.util;

public class Skin {
    private final String value, signature;
    
    protected Skin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getSignature() {
        return signature;
    }
}