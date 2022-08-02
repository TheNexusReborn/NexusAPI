package com.thenexusreborn.api.player;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.codec.PreferenceInfoCodec;

import java.util.*;

@TableInfo("preferences")
public class Preference {
    @Primary
    private long id;
    @ColumnInfo(name = "name", type = "varchar(100)", codec = PreferenceInfoCodec.class)
    private Info info;
    private UUID uuid;
    private boolean value;
    
    private Preference() {}
    
    public Preference(Info info, UUID uuid, boolean value) {
        this.info = info;
        this.value = value;
        this.uuid = uuid;
    }
    
    public Preference(Info info, UUID uuid, long id, boolean value) {
        this.info = info;
        this.id = id;
        this.value = value;
        this.uuid = uuid;
    }
    
    public boolean getValue() {
        return value;
    }
    
    public void setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;
        if (info.handler != null) {
            info.handler.handleChange(this, getPlayer(), oldValue, value);
        }
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public NexusPlayer getPlayer() {
        return NexusAPI.getApi().getPlayerManager().getNexusPlayer(uuid);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Info getInfo() {
        return info;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preference that = (Preference) o;
        return Objects.equals(info, that.info) && Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(info, uuid);
    }
    
    @TableInfo("preferenceinfo")
    public static class Info {
        @Primary private long id;
        private String name, displayName, description;
        private boolean defaultValue;
        @ColumnIgnored
        private Handler handler;
        
        private Info() {}
        
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
    
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Info info = (Info) o;
            return Objects.equals(name, info.name);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
    
    public interface Handler {
        void handleChange(Preference preference, NexusPlayer player, boolean oldValue, boolean newValue);
    }
}
