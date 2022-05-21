package com.thenexusreborn.api.networking.socket;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.networking.NetworkManager;
import com.thenexusreborn.api.server.ServerInfo;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

//This is a parent class for other handling
public abstract class NetworkSocket extends Thread {
    
    protected Socket socket;
    protected final AtomicBoolean active = new AtomicBoolean();
    protected long lastHeartbeat = 0;
    
    protected ServerInfo serverInfo;
    
    public boolean isActive() {
        return active.get();
    }
    
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) socket.getLocalSocketAddress();
    }
    
    public void sendCommand(String command) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(command);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        while (isActive()) {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Object object = in.readObject();
                if (object instanceof String) {
                    String cmd = (String) object;
                    if (cmd.startsWith("register")) {
                        NexusAPI.getApi().getLogger().info("Received register command");
                        String[] split = cmd.split(" ");
                        String serverName = split[2];
                        NexusAPI.getApi().getLogger().info("Determined Server Name to be \"" + serverName + "\"");
                        for (ServerInfo server : NexusAPI.getApi().getServerManager().getServers()) {
                            if (server.getName().equalsIgnoreCase(serverName)) {
                                this.serverInfo = server;
                                NexusAPI.getApi().getLogger().info("Registered connection to server " + server.getName());
                                break;
                            }
                        }
                    } else {
                        new Thread(() -> NetworkManager.getSocketCommandHandler().handleCommandInput((String) object)).start();
                    }
                }
            } catch (Exception e) {
                if (!(e.getMessage().contains("Connection reset"))) {
                    e.printStackTrace();
                }
                active.set(false);
            }
        }
        
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setActive(boolean active) {
        this.active.set(active);
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}
