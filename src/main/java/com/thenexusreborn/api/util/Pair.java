package com.thenexusreborn.api.util;

public record Pair<K, V>(K firstValue, V secondValue) {
    
    @Override
    public K firstValue() {
        return firstValue;
    }
    
    @Override
    public V secondValue() {
        return secondValue;
    }
}