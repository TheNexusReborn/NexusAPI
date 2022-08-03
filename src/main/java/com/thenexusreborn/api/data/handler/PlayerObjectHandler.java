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
        NexusPlayer player = (NexusPlayer) object;
        player.setPlayerProxy(NexusAPI.getApi().getPlayerFactory().createProxy(player));
    
        try {
            List<Preference> preferences = database.get(Preference.class, "uuid", player.getUniqueId());
            player.setPreferences(preferences);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<Stat> stats = new ArrayList<>(database.get(Stat.class, "uuid", player.getUniqueId()));
            
            for (Stat stat : stats) {
                player.addStat(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<StatChange> statChanges = database.get(StatChange.class, "uuid", player.getUniqueId());
            for (StatChange statChange : statChanges) {
                player.addStatChange(statChange);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<IPEntry> statChanges = database.get(IPEntry.class, "uuid", player.getUniqueId());
            for (IPEntry ipEntry : statChanges) {
                player.getIpHistory().add(ipEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void afterSave() {
        NexusPlayer player = (NexusPlayer) object;
    
        for (Preference preference : player.getPreferences().values()) {
            database.push(preference);
        }
    
        for (Stat stat : player.getStats().values()) {
            database.push(stat);
        }
    
        for (StatChange statChange : player.getStatChanges()) {
            database.push(statChange);
        }
    
        for (IPEntry ipEntry : player.getIpHistory()) {
            database.push(ipEntry);
            NexusAPI.getApi().getPlayerManager().getIpHistory().add(ipEntry);
        }
    }
}
