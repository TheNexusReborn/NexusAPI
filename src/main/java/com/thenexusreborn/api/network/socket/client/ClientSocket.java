package com.thenexusreborn.api.network.socket.client;

import com.thenexusreborn.api.network.socket.NexusSocket;

public class ClientSocket extends NexusSocket {
    public ClientSocket(String host, int port) {
        super(host, port);
    }
    
    @Override
    public void handleCommand(String command) {
        
    }
}
