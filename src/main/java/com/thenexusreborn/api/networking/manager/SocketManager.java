package com.thenexusreborn.api.networking.manager;

import com.thenexusreborn.api.networking.NetworkManager;
import com.thenexusreborn.api.networking.commands.SocketCommand;
import com.thenexusreborn.api.networking.socket.NetworkSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SocketManager {
    
    protected final ExecutorService executor = Executors.newFixedThreadPool(8);
    
    public abstract void init(String host, int port);
    
    public abstract NetworkSocket getSocket(String name);
    
    public abstract void sendSocketCommand(SocketCommand cmd, String sender, String... args);
    
    public void sendSocketCommand(String cmd, String sender, String... args) {
        SocketCommand command = NetworkManager.getSocketCommandHandler().getCommand(cmd);
        sendSocketCommand(command, sender, args);
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
