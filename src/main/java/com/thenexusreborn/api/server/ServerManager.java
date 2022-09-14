package com.thenexusreborn.api.server;

import com.thenexusreborn.api.NexusAPI;

import java.sql.SQLException;
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
    
    public List<ServerInfo> getServersByType(String type) {
        List<ServerInfo> servers = new ArrayList<>();
    
        for (ServerInfo server : new ArrayList<>(this.servers)) {
            if (server.getType().equalsIgnoreCase(type)) {
                servers.add(server);
            }
        }
        
        return servers;
    }
    
    public void addServer(int multicraftId) {
        try {
            this.servers.add(NexusAPI.getApi().getPrimaryDatabase().get(ServerInfo.class, "multicraftId", multicraftId).get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addServer(ServerInfo info) {
        this.servers.add(info);
    }
    
    public void updateStoredData() {
        try {
            List<ServerInfo> allServers = NexusAPI.getApi().getPrimaryDatabase().get(ServerInfo.class);
            for (ServerInfo server : new ArrayList<>(getServers())) {
                ServerInfo infoFromDatabase = null;
                for (ServerInfo s : allServers) {
                    if (s.getId() == server.getId()) {
                        infoFromDatabase = s;
                    }
                }
                if (infoFromDatabase != null) {
                    server.updateInfo(infoFromDatabase);
                }
            }
        
            for (ServerInfo server : allServers) {
                if (!this.servers.contains(server)) {
                    this.servers.add(server);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
