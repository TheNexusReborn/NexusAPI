package com.thenexusreborn.api.server;

public abstract non-sealed class InstanceServer extends NexusServer {
    protected ServerRegistry<VirtualServer> childServers = new ServerRegistry<>();
    
    public InstanceServer(String name, String mode) {
        super(name, ServerType.INSTANCE, mode);
    }

    public ServerRegistry<VirtualServer> getChildServers() {
        return childServers;
    }
}
