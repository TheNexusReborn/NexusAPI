package com.thenexusreborn.api.storage.handler;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;
import me.firestar311.starsql.api.objects.ObjectHandler;
import me.firestar311.starsql.api.objects.SQLDatabase;
import me.firestar311.starsql.api.objects.Table;

import java.sql.SQLException;
import java.util.*;

public class PlayerObjectHandler extends ObjectHandler {
    public PlayerObjectHandler(Object object, SQLDatabase database, Table table) {
        super(object, database, table);
    }
    
    @Override
    public void afterLoad() {
        NexusPlayer player = (NexusPlayer) object;
        player.setPlayerProxy(PlayerProxy.of(player.getUniqueId()));
    
        try {
            List<Toggle> toggles = database.get(Toggle.class, "uuid", player.getUniqueId());
            player.getToggles().setAll(toggles);
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

        try {
            List<Tag> tags = database.get(Tag.class, "uuid", player.getUniqueId());
            player.getTags().addAll(tags);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.getTags().setActive(player.getStatValue("tag").getAsString());
    }
    
    @Override
    public void afterSave() {
        NexusPlayer player = (NexusPlayer) object;
    
        for (Toggle toggle : player.getToggles().findAll()) {
            database.saveSilent(toggle);
        }
    
        for (Stat stat : player.getStats().findAll()) {
            database.saveSilent(stat);
        }
    
        for (StatChange statChange : player.getStats().findAllChanges()) {
            database.saveSilent(statChange);
        }
    
        for (IPEntry ipEntry : player.getIpHistory()) {
            database.saveSilent(ipEntry);
            NexusAPI.getApi().getPlayerManager().getIpHistory().add(ipEntry);
        }
    }
}
