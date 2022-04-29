package com.thenexusreborn.api.tags;

import java.util.*;

public class TagManager {
    
    private Map<String, Tag> tags = new HashMap<>();
    
    public TagManager() {
        addTag(new Tag("dev"));
        addTag(new Tag("prealpha"));
        addTag(new Tag("alpha"));
        addTag(new Tag("beta"));
        addTag(new Tag("thicc"));
        addTag(new Tag("son"));
        addTag(new Tag("e-girl"));
        addTag(new Tag("god"));
        addTag(new Tag("e-dater"));
        addTag(new Tag("lord"));
        addTag(new Tag("epic"));
        addTag(new Tag("bacca"));
        addTag(new Tag("benja"));
        addTag(new Tag("milk man"));
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
