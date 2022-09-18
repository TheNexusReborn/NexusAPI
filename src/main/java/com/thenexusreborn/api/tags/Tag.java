package com.thenexusreborn.api.tags;

import java.util.*;

public class Tag {
    
    public static final Set<String> presetTags;
    
    static {
        presetTags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("thicc", "son", "e-girl", "god", "e-dater", "lord", "epic", "bacca", "benja", "milk man", "champion")));
    }
    
    private final String name;
    
    public Tag(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        if (this.name != null) {
            return "&d&l" + this.name.toUpperCase();
        } else {
            return "";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
