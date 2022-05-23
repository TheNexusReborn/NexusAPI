package com.thenexusreborn.api.network.cmd;

import com.thenexusreborn.api.network.socket.NexusSocket;

public class SocketCommand {
    protected String name;
    protected String[] aliases;
    
    public SocketCommand(String name, String[] aliases) {
        this.name = name;
        this.aliases = aliases;
    }
    
    public SocketCommand(String name) {
        this.name = name;
    }
    
    /*
    The socket is the connection from where it was received, but may not be the original source as the ServerSocket will forward messages
    The source is taken from the command itself as the syntax is <cmd> <source> <args> 
    The args are derived from the rest
     */
    public void onCommand(NexusSocket socket, String source, String[] args) {
        
    }
}
