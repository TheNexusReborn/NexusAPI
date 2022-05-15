package com.thenexusreborn.api.networking.socket;

import com.thenexusreborn.api.server.ServerInfo;

import java.net.*;

//This is a handler class for handling server socket connections on the server side
public class HandlerSocket extends NetworkSocket {

    private final NetworkServerSocket serverSocket;
    
    private ServerInfo serverInfo;

    public HandlerSocket(Socket socket, NetworkServerSocket serverSocket) {
        this.socket = socket;
        this.serverSocket = serverSocket;
        active.set(true);
    }
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}
