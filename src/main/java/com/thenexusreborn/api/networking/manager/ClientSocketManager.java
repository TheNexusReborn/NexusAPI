package com.thenexusreborn.api.networking.manager;

import com.thenexusreborn.api.helper.StringHelper;
import com.thenexusreborn.api.networking.NetworkManager;
import com.thenexusreborn.api.networking.commands.SocketCommand;
import com.thenexusreborn.api.networking.socket.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ClientSocketManager extends SocketManager {
    
    private ClientSocket socket;
    
    public void init(String host, int port) {
        this.socket = new ClientSocket(host, port);
        this.socket.connect();
        this.socket.setDaemon(false);
        System.out.println("Connected to the socket");
        this.socket.start();

        Thread thread = new Thread(() -> {
            while (socket.isActive()) {
                SocketCommand heartbeatCmd = NetworkManager.getSocketCommandHandler().getCommand("heartbeat");
                sendSocketCommand(heartbeatCmd, "test", null);
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    public NetworkSocket getSocket(String name) {
        return socket;
    }

    public void sendSocketCommand(SocketCommand cmd, String sender, String[] args) {
        String output = cmd.getName() + " " + sender;
        if (args != null) {
            output += " " + StringHelper.join(Arrays.asList(args), " "); 
        }
        socket.sendCommand(output);
    }
}
