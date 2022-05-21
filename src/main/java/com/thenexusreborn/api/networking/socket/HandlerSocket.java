package com.thenexusreborn.api.networking.socket;

import com.thenexusreborn.api.server.ServerInfo;

import java.net.*;

//This is a handler class for handling server socket connections on the server side
public class HandlerSocket extends NetworkSocket {

    private final NetworkServerSocket serverSocket;
    

    public HandlerSocket(Socket socket, NetworkServerSocket serverSocket) {
        this.socket = socket;
        this.serverSocket = serverSocket;
        active.set(true);
    }
}
