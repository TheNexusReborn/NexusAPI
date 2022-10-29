package com.thenexusreborn.api.player;

import com.thenexusreborn.api.tags.Tag;

import java.util.*;

public class PlayerTags {
    private final Map<String, Tag> tags = new HashMap<>();
    private String active;

    public Tag get(String name) {
        return tags.get(name);
    }

    public void add(Tag tag) {
        this.tags.put(tag.getName(), tag);
    }

    public Tag getActive() {
        return get(active);
    }

    public void setActive(String active) {
        if (this.tags.containsKey(active)) {
            this.active = active;
        }
    }

    public boolean hasActiveTag() {
        return active != null && !active.equals("") && !active.equals("null");
    }

    public void remove(String tag) {
        this.tags.remove(tag);
    }

    public void addAll(List<Tag> tags) {
        tags.forEach(this::add);
    }

    public boolean isUnlocked(String tag) {
        return this.tags.containsKey(tag);
    }

    public Set<String> findAll() {
        return new HashSet<>(this.tags.keySet());
    }
}
