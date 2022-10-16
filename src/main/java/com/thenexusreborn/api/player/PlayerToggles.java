package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.Toggle.Info;

import java.util.*;

public class PlayerToggles {
    private final Map<String, Toggle> toggles = new HashMap<>();
    
    public Toggle get(String name) {
        return this.toggles.get(name.toLowerCase());
    }
    
    public void add(Toggle toggle) {
        this.toggles.put(toggle.getInfo().getName().toLowerCase(), toggle);
    }
    
    public void setValue(String name, boolean value) {
        Toggle toggle = get(name);
        if (toggle != null) {
            toggle.setValue(value);
        }
    }
    
    public boolean getValue(String name) {
        Toggle toggle = get(name);
        if (toggle != null) {
            return toggle.getValue();
        } else {
            Info info = NexusAPI.getApi().getToggleRegistry().get(name.toLowerCase());
            if (info != null) {
                return info.getDefaultValue();
            } else {
                throw new IllegalArgumentException("Invalid toggle name: " + name);
            }
        }
    }
    
    public void setAll(List<Toggle> toggles) {
        this.toggles.clear();
        for (Toggle toggle : toggles) {
            add(toggle);
        }
    }
    
    public List<Toggle> findAll() {
        return new ArrayList<>(this.toggles.values());
    }
}
