package com.thenexusreborn.api.networking.commands;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.StringHelper;
import com.thenexusreborn.api.networking.NetworkManager;
import com.thenexusreborn.api.networking.manager.*;
import com.thenexusreborn.api.networking.socket.*;
import com.thenexusreborn.api.server.ServerInfo;

import java.util.*;

public class SocketCommandHandler {
    private final List<SocketCommand> commands = Collections.synchronizedList(new ArrayList<>());

    public SocketCommandHandler() {
        addCommand(new SocketCommand("heartbeat", null, "Command to periodically check if a socket connection is active.").setExecutor((cmd, sender, args) -> {
            SocketManager socketManager = NetworkManager.getSocketManager();
            NetworkSocket socket = socketManager.getSocket(sender);
            socket.setLastHeartbeat(System.currentTimeMillis());
        }));
        
        addCommand(new SocketCommand("close", null, "Command to gracefully close the connection.").setExecutor((cmd, sender, args) -> {
            SocketManager socketManager = NetworkManager.getSocketManager();
            NetworkSocket socket = socketManager.getSocket(sender);
            socket.close();
        }));
        
        addCommand(new SocketCommand("register", null, "Command to tell the proxy which server a connection currently is.").setExecutor((cmd, sender, args) -> {
            String serverName = StringHelper.join(args, " ");
            SocketManager socketManager = NetworkManager.getSocketManager();
            NetworkSocket socket = socketManager.getSocket(serverName);
            if (socket instanceof HandlerSocket) {
                HandlerSocket handlerSocket = (HandlerSocket) socket;
                for (ServerInfo server : NexusAPI.getApi().getServerManager().getServers()) {
                    if (server.getName().equalsIgnoreCase(serverName)) {
                        handlerSocket.setServerInfo(server);
                        NexusAPI.getApi().getLogger().info("Registered connection to server " + server.getName());
                        break;
                    }
                }
                
                if (handlerSocket.getServerInfo() == null) {
                    NexusAPI.getApi().getDataManager().getAllServersAsync(servers -> {
                        for (ServerInfo server : servers) {
                            if (server.getName().equalsIgnoreCase(serverName)) {
                                handlerSocket.setServerInfo(server);
                                NexusAPI.getApi().getLogger().info("Registered connection to server " + server.getName());
                                break;
                            }
                        }
                    });
                }
            }
        }));
    }

    public SocketCommand getCommand(String name) {
        for (SocketCommand cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            } else {
                if (cmd.getAliases() != null) {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(name)) {
                            return cmd;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void addCommand(Object object) {
        if (object instanceof SocketCommand) {
            commands.add((SocketCommand) object);
        } else if (object.getClass().isAnnotationPresent(SocketCmd.class)) {
            SocketCmd cmdInfo = object.getClass().getAnnotation(SocketCmd.class);
            if (cmdInfo != null) {
                SocketCommand socketCommand = new SocketCommand(cmdInfo.name(), cmdInfo.aliases(), cmdInfo.description());
                commands.add(socketCommand);
            }
        }
    }

    public void handleCommandInput(String message) {
        String[] messageSplit = message.split(" ");
        String server;
        String[] args;
        if (messageSplit.length < 3)
            return;
        SocketCommand command = getCommand(messageSplit[0]);
        server = messageSplit[1];
        args = new String[messageSplit.length - 2];
        System.arraycopy(messageSplit, 2, args, 0, args.length);
        if (command == null)
            return;
        if (command.getExecutor() != null) {
            command.getExecutor().onCommand(command, server, args);
        }
    
        SocketManager socketManager = NexusAPI.getApi().getSocketManager();
        if (socketManager instanceof ServerSocketManager) {
            ServerSocketManager serverSocketManager = (ServerSocketManager) socketManager;
            serverSocketManager.forwardCommand(message);
        }
    }
}
