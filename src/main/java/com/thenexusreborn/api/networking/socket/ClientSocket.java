package com.thenexusreborn.api.networking.socket;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.networking.manager.SocketManager;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

//This represents a socket on a bungee proxy or a spigot server connected to the server socket
public class ClientSocket extends NetworkSocket {
    
    public static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(1);
    private final String host;
    private final int port;

    private long lastHeartbeat;
    
    public ClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void connect() {
        NexusAPI.getApi().getLogger().info("Attempting a socket connection to " + host + ":" + port);
        try {
            this.socket = new Socket(host, port);
            active.set(true);
            String serverName = NexusAPI.getApi().getServerManager().getCurrentServer().getName();
            System.out.println("Server Name: " + serverName);
            SocketManager socketManager = NexusAPI.getApi().getSocketManager();
            System.out.println("Socket Manager: " + socketManager);
            socketManager.sendSocketCommand("register", serverName.toLowerCase().replace(" ", "_"), serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
}
