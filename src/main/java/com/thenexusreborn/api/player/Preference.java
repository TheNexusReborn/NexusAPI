package com.thenexusreborn.api.player;

public class Preference {
    private final Info info; //In theory, this should go based off of one instance so if that changes, then they all should change if they have the same
    private int id; //Database ID
    private boolean value;
    
    public Preference(Info info, boolean value) {
        this.info = info;
        this.value = value;
    }
    
    public Preference(Info info, int id, boolean value) {
        this.info = info;
        this.id = id;
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    public void setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;
        if (info.handler != null) {
            info.handler.handleChange(this, oldValue, value);
        }
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Info getInfo() {
        return info;
    }
    
    public static class Info {
        private final String name, displayName, description;
        private final boolean defaultValue;
        private Handler handler;
    
        public Info(String name, String displayName, String description, boolean defaultValue) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.defaultValue = defaultValue;
        }
    
        public String getName() {
            return name;
        }
    
        public String getDisplayName() {
            return displayName;
        }
    
        public String getDescription() {
            return description;
        }
    
        public boolean getDefaultValue() {
            return defaultValue;
        }
    
        public Handler getHandler() {
            return handler;
        }
    
        public void setHandler(Handler handler) {
            this.handler = handler;
        }
    }
    
    public interface Handler {
        void handleChange(Preference preference, boolean oldValue, boolean newValue);
    }
}
