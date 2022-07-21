package com.thenexusreborn.api.player;

import com.thenexusreborn.api.data.annotations.TableInfo;

import java.util.UUID;

@TableInfo("iphistory")
public class IPEntry {
    private String ip;
    private UUID uuid;
    
    private IPEntry() {}
    
    public IPEntry(String ip, UUID uuid) {
        this.ip = ip;
        this.uuid = uuid;
    }
    
    public String getIp() {
        return ip;
    }
    
    public UUID getUuid() {
        return uuid;
    }
}
