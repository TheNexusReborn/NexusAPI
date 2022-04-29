package com.thenexusreborn.api.tags;

import java.util.Objects;

public class Tag {
    private final String name;
    
    public Tag(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return "&d&l" + this.name.toUpperCase();
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
