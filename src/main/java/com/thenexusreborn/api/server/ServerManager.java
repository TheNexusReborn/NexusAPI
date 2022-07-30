package com.thenexusreborn.api.server;

import com.thenexusreborn.api.NexusAPI;

import java.util.*;

public abstract class ServerManager {
    
    protected ServerInfo currentServer;
    protected final List<ServerInfo> servers = new ArrayList<>();
    
    public abstract void setupCurrentServer();
    
    public ServerInfo getCurrentServer() {
        return currentServer;
    }
    
    public List<ServerInfo> getServers() {
        return servers;
    }
    
    public void addServer(int multicraftId) {
        this.servers.add(NexusAPI.getApi().getDataManager().getServerInfo(multicraftId));
    }
    
    public void addServer(ServerInfo info) {
        this.servers.add(info);
    }
    
    public void updateStoredData() {
        NexusAPI.getApi().getThreadFactory().runAsync(() -> NexusAPI.getApi().getDataManager().updateAllServers(new ArrayList<>(servers)));
    }
}
