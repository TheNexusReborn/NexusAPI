package com.thenexusreborn.api.network.cmd;

public class NetworkCommand {
    protected String name;
    protected NetworkCommandExecutor executor;
    
    public NetworkCommand(String name) {
        this.name = name;
    }
    
    public NetworkCommand(String name, NetworkCommandExecutor executor) {
        this.name = name;
        this.executor = executor;
    }
    
    public String getName() {
        return name;
    }
    
    public NetworkCommandExecutor getExecutor() {
        return executor;
    }
    
    public void setExecutor(NetworkCommandExecutor executor) {
        this.executor = executor;
    }
}
