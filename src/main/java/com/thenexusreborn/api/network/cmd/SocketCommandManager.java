package com.thenexusreborn.api.network.cmd;

import java.util.*;

public class SocketCommandManager {
    private Map<String, SocketCommand> commands = new HashMap<>();
    
    public void registerCommand(SocketCommand command) {
        this.commands.put(command.getName().toLowerCase(), command);
    }
    
    public SocketCommand getCommand(String name) {
        if (commands.containsKey(name.toLowerCase())) {
            return commands.get(name.toLowerCase());
        }
    
        for (SocketCommand cmd : this.commands.values()) {
            if (cmd.getAliases() != null) {
                for (String alias : cmd.getAliases()) {
                    if (alias.equalsIgnoreCase(name)) {
                        return cmd;
                    }
                }
            }
        }
        
        
        return null;
    }
}
