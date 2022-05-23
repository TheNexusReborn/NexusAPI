package com.thenexusreborn.api.network.socket.server;

import com.thenexusreborn.api.network.socket.NexusSocket;

import java.io.IOException;
import java.net.Socket;

public class HandlerSocket extends NexusSocket {
    public HandlerSocket(Socket socket) {
        super(socket);
    }
    
    @Override
    public void connect() throws IOException {
        throw new IOException("Cannot connect to a handler socket.");
    }
}