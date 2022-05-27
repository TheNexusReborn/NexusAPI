package com.thenexusreborn.api.network;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.network.netty.app.*;
import com.thenexusreborn.api.network.netty.model.NexusPacket;

import java.util.*;

public class NetworkManager {
    
    private Map<String, NetworkCommand> commandMap = new HashMap<>();
    
    private NetworkContext context;
    
    private NettyApp nettyApp;
    
    public NetworkManager(NetworkContext context) {
        this.context = context;
    }
    
    public void init(String host, int port) throws Exception {
        if (context == NetworkContext.CLIENT) {
            nettyApp = new NettyClient(host, port);
        } else {
            nettyApp = new NettyServer(host, port);
        }
        
        nettyApp.init();
    }
    
    public void addCommand(NetworkCommand command) {
        this.commandMap.put(command.getName().toLowerCase(), command);
    }
    
    public NetworkCommand getCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }
    
    public void send(String action, String[] data) {
        nettyApp.send(NexusAPI.getApi().getServerManager().getCurrentServer().getName(), action, data);
    }
    
    public void handleInboundPacket(NexusPacket packet) {
        if (context == NetworkContext.SERVER) {
            nettyApp.send(packet);
        }
        
        NetworkCommand command = getCommand(packet.getAction());
        if (command != null) {
            if (command.getExecutor() != null) {
                command.getExecutor().handle(command, packet.getOrigin(), packet.getData());
            } else {
                NexusAPI.getApi().getLogger().warning("Command " + command.getName() + " does not have an executor.");
            }
        }
    }
}