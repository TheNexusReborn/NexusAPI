package com.thenexusreborn.api.network.socket.server;

import java.io.IOException;
import java.net.ServerSocket;

public class NexusServerSocket extends Thread {
    private ServerSocket serverSocket;
    
    private int port;
    
    public NexusServerSocket(int port) {
        this.port = port;
    }
    
    public void connect() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }
}
