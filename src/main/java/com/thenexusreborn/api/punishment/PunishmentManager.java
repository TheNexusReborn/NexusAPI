package com.thenexusreborn.api.punishment;

import java.util.*;

public class PunishmentManager {
    
    private Map<Integer, Punishment> punishments = new HashMap<>();
    
    public void addPunishment(Punishment punishment) {
        this.punishments.put(punishment.getId(), punishment);
    }
    
    public Punishment getPunishment(int id) {
        return punishments.get(id);
    }
    
    public Punishment getPunishmentByTarget(UUID target) {
        for (Punishment punishment : this.punishments.values()) {
            if (punishment.getTarget().equalsIgnoreCase(target.toString())) {
                return punishment;
            }
        }
        
        return null;
    }
    
    public List<Punishment> getPunishmentsByTarget(UUID target) {
        List<Punishment> punishments = new ArrayList<>();
        
        for (Punishment punishment : this.punishments.values()) {
            if (punishment.getTarget().equalsIgnoreCase(target.toString())) {
                punishments.add(punishment);
            }
        }
        
        return punishments;
    }
}
