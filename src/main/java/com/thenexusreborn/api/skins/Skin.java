package com.thenexusreborn.api.skins;

public class Skin {
    private final String identifier, value, signature;
    
    public Skin(String identifier, String value, String signature) {
        this.identifier = identifier;
        this.value = value;
        this.signature = signature;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getIdentifier() {
        return identifier;
    }
}