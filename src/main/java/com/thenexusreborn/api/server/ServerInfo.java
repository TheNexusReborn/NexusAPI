package com.thenexusreborn.api.server;

import com.thenexusreborn.api.data.annotations.Primary;

import java.util.Objects;

public class ServerInfo {
    @Primary
    private long id;
    private String ip, name;
    private int port, multicraftId;
    private int players, maxPlayers, hiddenPlayers;
    private String type, status, state;
    
    private ServerInfo() {}
    
    public ServerInfo(int multicraftId, String ip, String name, int port) {
        this(multicraftId, ip, name, port, 0, 0, 0, "", "offline", "none");
    }
    
    public ServerInfo(int multicraftId, String ip, String name, int port, int players, int maxPlayers, int hiddenPlayers, String type, String status, String state) {
        this.ip = ip;
        this.name = name;
        this.port = port;
        this.multicraftId = multicraftId;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.hiddenPlayers = hiddenPlayers;
        this.type = type;
        this.status = status;
        this.state = state;
    }
    
    public String getIp() {
        return ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getMulticraftId() {
        return multicraftId;
    }
    
    public int getPlayers() {
        return players;
    }
    
    public void setPlayers(int players) {
        this.players = players;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public int getHiddenPlayers() {
        return hiddenPlayers;
    }
    
    public void setHiddenPlayers(int hiddenPlayers) {
        this.hiddenPlayers = hiddenPlayers;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getName() {
        return name;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
