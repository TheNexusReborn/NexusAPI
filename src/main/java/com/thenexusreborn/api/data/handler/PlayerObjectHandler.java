package com.thenexusreborn.api.data.handler;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.objects.*;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.stats.*;

import java.sql.SQLException;
import java.util.*;

public class PlayerObjectHandler extends ObjectHandler {
    public PlayerObjectHandler(Object object, Database database, Table table) {
        super(object, database, table);
    }
    
    @Override
    public void afterLoad() {
        CachedPlayer cachedPlayer = (CachedPlayer) object;
    
        try {
            List<Preference> preferences = database.get(Preference.class, "uuid", cachedPlayer.getUniqueId());
            cachedPlayer.setPreferences(preferences);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<Stat> stats = new ArrayList<>(database.get(Stat.class, new String[]{"uuid", "name"}, new Object[]{cachedPlayer.getUniqueId(), "online"}));
            stats.addAll(database.get(Stat.class, new String[]{"uuid", "name"}, new Object[]{cachedPlayer.getUniqueId(), "server"}));
            stats.addAll(database.get(Stat.class, new String[]{"uuid", "name"}, new Object[]{cachedPlayer.getUniqueId(), "online"}));
            stats.addAll(database.get(Stat.class, new String[]{"uuid", "name"}, new Object[]{cachedPlayer.getUniqueId(), "unlockedtags"}));
            stats.addAll(database.get(Stat.class, new String[]{"uuid", "name"}, new Object[]{cachedPlayer.getUniqueId(), "tag"}));
            //TODO add more based on commands and stuff
            
            for (Stat stat : stats) {
                cachedPlayer.addStat(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<StatChange> statChanges = database.get(StatChange.class, "uuid", cachedPlayer.getUniqueId());
            for (StatChange statChange : statChanges) {
                cachedPlayer.addStatChange(statChange);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        for (IPEntry ipEntry : NexusAPI.getApi().getPlayerManager().getIpHistory()) {
            if (ipEntry.getUuid().equals(cachedPlayer.getUniqueId())) {
                cachedPlayer.getIpHistory().add(ipEntry);
            }
        }
    }
    
    @Override
    public void afterSave() {
        CachedPlayer cachedPlayer = (CachedPlayer) object;
    
        for (Preference preference : cachedPlayer.getPreferences().values()) {
            database.push(preference);
        }
    
        for (Stat stat : cachedPlayer.getStats().values()) {
            database.push(stat);
        }
    
        for (StatChange statChange : cachedPlayer.getStatChanges()) {
            database.push(statChange);
        }
    }
}
