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
            List<Toggle> toggles = database.get(Toggle.class, "uuid", player.getUniqueId());
            player.getToggles().setAll(toggles);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<Stat> stats = new ArrayList<>(database.get(Stat.class, "uuid", player.getUniqueId()));
            
            for (Stat stat : stats) {
                player.getStats().add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        try {
            List<StatChange> statChanges = database.get(StatChange.class, "uuid", player.getUniqueId());
            for (StatChange statChange : statChanges) {
                player.getStats().addChange(statChange);
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
    
        for (Toggle preference : player.getToggles().findAll()) {
            database.push(preference);
        }
    
        for (Stat stat : player.getStats().findAll()) {
            database.push(stat);
        }
    
        for (StatChange statChange : player.getStats().findAllChanges()) {
            database.push(statChange);
        }
    
        for (IPEntry ipEntry : player.getIpHistory()) {
            database.push(ipEntry);
            NexusAPI.getApi().getPlayerManager().getIpHistory().add(ipEntry);
        }
    }
}
