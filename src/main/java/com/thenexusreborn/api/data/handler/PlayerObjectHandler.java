package com.thenexusreborn.api.data.handler;

import com.thenexusreborn.api.data.objects.*;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.stats.*;

import java.sql.SQLException;
import java.util.List;

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
            List<Stat> stats = database.get(Stat.class, "uuid", cachedPlayer.getUniqueId());
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
