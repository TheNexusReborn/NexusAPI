package com.thenexusreborn.api.network;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.network.netty.model.NexusPacket;

import java.util.*;

public class NetworkManager {
    
    private final Map<String, NetworkCommand> commandMap = new HashMap<>();
    
    private final NetworkContext context;
    
    public NetworkManager(NetworkContext context) {
        this.context = context;
    }
    
    public void init(String host, int port) {
        
    }
    
    public void addCommand(NetworkCommand command) {
        this.commandMap.put(command.getName().toLowerCase(), command);
    }
    
    public NetworkCommand getCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }
    
    public void send(String action, String... data) {
        
    }
    
    public void handleInboundPacket(NexusPacket packet) {
        NetworkCommand command = getCommand(packet.action());
        if (command != null) {
            if (command.getExecutor() != null) {
                command.getExecutor().handle(command, packet.origin(), packet.data());
            } else {
                NexusAPI.getApi().getLogger().warning("Command " + command.getName() + " does not have an executor.");
            }
        }
    }
    
    public void close() {
        
    }
}