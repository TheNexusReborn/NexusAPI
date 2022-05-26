package com.thenexusreborn.api.network;

import com.thenexusreborn.api.network.cmd.SocketCommandManager;

public class NetworkManager {
    private SocketCommandManager socketCommandManager;
    
    public NetworkManager() {
        this.socketCommandManager = new SocketCommandManager();
    }
    
    public SocketCommandManager getSocketCommandManager() {
        return socketCommandManager;
    }
}
