package com.thenexusreborn.api.networking.manager;

import com.thenexusreborn.api.helper.StringHelper;
import com.thenexusreborn.api.networking.commands.SocketCommand;
import com.thenexusreborn.api.networking.socket.*;

import java.util.Arrays;

public class ServerSocketManager extends SocketManager {
    
    private NetworkServerSocket socket;
    
    public void init(String host, int port) {
        socket = new NetworkServerSocket(host, port);
        socket.start();
    }

    public NetworkSocket getSocket(String name) {
        for (HandlerSocket handler : socket.getHandlers()) {
            if (handler.getServerInfo().getName().toLowerCase().replace(" ", "_").equalsIgnoreCase(name)) {
                return handler;
            }
        }
        return null;
    }

    public void sendSocketCommand(SocketCommand cmd, String sender, String[] args) {
        socket.sendCommand(cmd.getName() + " " + sender + " " + StringHelper.join(Arrays.asList(args), " "));
    }
    
    public void forwardCommand(String command) {
        socket.sendCommand(command);
    }
}
