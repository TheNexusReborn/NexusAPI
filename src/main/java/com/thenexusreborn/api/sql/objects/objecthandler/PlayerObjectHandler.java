package com.thenexusreborn.api.sql.objects.objecthandler;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.experience.PlayerExperience;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.sql.objects.ObjectHandler;
import com.thenexusreborn.api.sql.objects.SQLDatabase;
import com.thenexusreborn.api.sql.objects.Table;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.tags.Tag;

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
            PlayerExperience experience = database.get(PlayerExperience.class, "uniqueid", player.getUniqueId().toString()).get(0);
            player.getExperience().setLevel(experience.getLevel());
            player.getExperience().setLevelXp(experience.getLevelXp());
        } catch (Exception e) {
            if (e instanceof SQLException) {
                e.printStackTrace();
            }
        }

        try {
            PlayerTime playerTime = database.get(PlayerTime.class, "uniqueid", player.getUniqueId().toString()).get(0);
            player.getPlayerTime().setFirstJoined(playerTime.getFirstJoined());
            player.getPlayerTime().setLastLogin(playerTime.getLastLogin());
            player.getPlayerTime().setLastLogout(playerTime.getLastLogout());
            player.getPlayerTime().setPlaytime(playerTime.getPlaytime());
        } catch (Exception e) {
            if (e instanceof SQLException) {
                e.printStackTrace();
            }
        }

        try {
            PlayerBalance balance = database.get(PlayerBalance.class, "uniqueid", player.getUniqueId().toString()).get(0);
            player.getBalance().setCredits(balance.getCredits());
            player.getBalance().setNexites(balance.getNexites());
        } catch (Exception e) {
            if (e instanceof SQLException) {
                e.printStackTrace();
            }
        }
    
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
            List<IPEntry> ipEntries = database.get(IPEntry.class, "uuid", player.getUniqueId());
            for (IPEntry ipEntry : ipEntries) {
                player.getIpHistory().add(ipEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            List<Tag> tags = database.get(Tag.class, "uuid", player.getUniqueId());
            player.addAllTags(tags);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.setActiveTag(player.getStatValue("tag").getAsString());
    }
    
    @Override
    public void afterSave() {
        NexusPlayer player = (NexusPlayer) object;
        
        if (player.getExperience() != null) {
            database.saveSilent(player.getExperience());
        }
    
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
