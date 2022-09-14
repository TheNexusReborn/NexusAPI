package com.thenexusreborn.api.registry;

import java.util.*;

public abstract class Registry<T> {
    private final Set<T> objects = new HashSet<>();
    
    public void register(T object) {
        if (!objects.contains(object)) {
            this.objects.add(object);
        }
    }
    
    public Set<T> getObjects() {
        return objects;
    }
    
    public void remove(T object) {
        this.objects.remove(object);
    }
    
    public abstract T get(String str);
}
