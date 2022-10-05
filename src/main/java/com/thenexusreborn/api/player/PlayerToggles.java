package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.Toggle.Info;

import java.util.*;

public class PlayerToggles {
    private final Map<String, Toggle> toggles = new HashMap<>();
    
    public Toggle get(String name) {
        return this.toggles.get(name.toLowerCase());
    }
    
    public void add(Toggle preference) {
        this.toggles.put(preference.getInfo().getName().toLowerCase(), preference);
    }
    
    public void setValue(String name, boolean value) {
        Toggle preference = get(name);
        if (preference != null) {
            preference.setValue(value);
        }
    }
    
    public boolean getValue(String name) {
        Toggle toggle = get(name);
        if (toggle != null) {
            return toggle.getValue();
        } else {
            Info info = NexusAPI.getApi().getPreferenceRegistry().get(name.toLowerCase());
            if (info != null) {
                return info.getDefaultValue();
            } else {
                throw new IllegalArgumentException("Invalid toggle name: " + name);
            }
        }
    }
    
    public void setAll(List<Toggle> preferences) {
        this.toggles.clear();
        for (Toggle preference : preferences) {
            add(preference);
        }
    }
    
    public List<Toggle> findAll() {
        return new ArrayList<>(this.toggles.values());
    }
}
