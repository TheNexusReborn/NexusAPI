package com.thenexusreborn.api.networking;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.networking.commands.SocketCommandHandler;
import com.thenexusreborn.api.networking.manager.*;

public class NetworkManager {

    private static SocketCommandHandler socketCommandHandler;
    private static SocketManager socketManager;
    private static String host;
    private static int port;
    
    public static SocketManager create(SocketContext context, String host, int port) {
        if (context == SocketContext.SERVER) {
            socketManager = new ServerSocketManager();
        } else if (context == SocketContext.CLIENT) {
            socketManager = new ClientSocketManager();
        } else {
            NexusAPI.getApi().getLogger().severe("Could not find a valid socket context");
            return null;
        }
        
        NetworkManager.host = host;
        NetworkManager.port = port;

        socketCommandHandler = new SocketCommandHandler();
        NexusAPI.getApi().getLogger().info("Initiated a socket with the context " + context.name());
        return socketManager;
    }
    
    public static void init() {
        socketManager.init(host, port);
    }
    
    public static SocketCommandHandler getSocketCommandHandler() {
        return socketCommandHandler;
    }

    public static SocketManager getSocketManager() {
        return socketManager;
    }
}
