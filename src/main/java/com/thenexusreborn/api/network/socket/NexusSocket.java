package com.thenexusreborn.api.network.socket;

import java.io.IOException;
import java.net.*;

public abstract class NexusSocket extends Thread {
    private Socket socket;
    
    private String host;
    private int port;
    
    public NexusSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public NexusSocket(Socket socket) {
        this.socket = socket;
    }
    
    public void connect() throws IOException {
        this.socket = new Socket(host, port);
    }
}
