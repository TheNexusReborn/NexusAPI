package com.thenexusreborn.api.tags;

import java.util.*;

public class TagManager {
    
    private Map<String, Tag> tags = new HashMap<>();
    
    public TagManager() {
        addTag(new Tag("dev"));
        addTag(new Tag("prealpha"));
    }
    
    public Tag getTag(String name) {
        return tags.get(name.toLowerCase());
    }
    
    public void addTag(Tag tag) {
        this.tags.put(tag.getName().toLowerCase(), tag);
    }
    
    public Map<String, Tag> getTags() {
        return tags;
    }
}
