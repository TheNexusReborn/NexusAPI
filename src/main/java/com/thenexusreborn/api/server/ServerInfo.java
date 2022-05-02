package com.thenexusreborn.api.server;

import java.util.Objects;

/**
 * The IP and Port is taken from the ServerProperties utility
 * The Multicraft id is set via a config
 * The players, maxPlayers and hiddenPlayers are updated by the server
 * Status comes from both the server and the MulticraftAPI
 * The MulticraftAPI will determine if the server is online, if it is, then it will respect the status in the database
 * The type is just a string for the different types like Proxy, Hub, SG and any others. There will not be an enum
 * The state is mostly for game servers that use a State enum like in SG with LobbyState and GameState
 * For example, an SG server might do the following
 * When online, and just started, it will have the status "Lobby" and the state "Waiting" 
 * In a game, it would do "Game" and whatever the current state is.
 * For the state, it will only push on some game states, not all, that is way too much IO
 */
public class ServerInfo {
    private final String ip, name;
    private final int port, multicraftId;
    private int players, maxPlayers, hiddenPlayers;
    private String type, status, state;
    
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
