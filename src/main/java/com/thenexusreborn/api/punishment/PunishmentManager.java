package com.thenexusreborn.api.punishment;

import com.thenexusreborn.api.*;

import java.util.*;
import java.util.Map.Entry;

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
    
        Map<String, Set<UUID>> ipHistory = new HashMap<>(NexusAPI.getApi().getPlayerManager().getIpHistory());
        List<String> methodTargetIPs = getIpHistory(ipHistory, target);
    
        for (Punishment punishment : this.punishments.values()) {
            UUID punishmentTarget = UUID.fromString(punishment.getTarget());
            if (punishmentTarget.equals(target)) {
                punishments.add(punishment);
            } else if (punishment.getType() == PunishmentType.BLACKLIST) {
                List<String> punishmentTargetIPs = getIpHistory(ipHistory, punishmentTarget);
                List<String> sharedIps = new ArrayList<>();
                for (String methodTargetIP : methodTargetIPs) {
                    for (String punishmentTargetIP : punishmentTargetIPs) {
                        if (methodTargetIP.equalsIgnoreCase(punishmentTargetIP)) {
                            sharedIps.add(methodTargetIP);
                        }
                    }
                }
    
                if (sharedIps.size() > 0) {
                    punishments.add(punishment);
                }
            }
        }
        
        return punishments;
    }
    
    private List<String> getIpHistory(Map<String, Set<UUID>> ipHistory, UUID punishmentTarget) {
        List<String> ips = new ArrayList<>();
        for (Entry<String, Set<UUID>> entry : ipHistory.entrySet()) {
            for (UUID uuid : entry.getValue()) {
                if (uuid.equals(punishmentTarget)) {
                    ips.add(entry.getKey());
                }
            }
        }
        return ips;
    }
    
    public List<Punishment> getPunishments() {
        return new ArrayList<>(this.punishments.values());
    }
}
