package com.thenexusreborn.api.server;

public abstract non-sealed class InstanceServer extends NexusServer {

    protected VirtualServer primaryVirtualServer;
    protected ServerRegistry<VirtualServer> childServers = new ServerRegistry<>();
    
    public InstanceServer(String name, String mode, int maxPlayers) {
        super(name, ServerType.INSTANCE, mode, maxPlayers);
    }

    public ServerRegistry<VirtualServer> getChildServers() {
        return childServers;
    }

    public VirtualServer getPrimaryVirtualServer() {
        return primaryVirtualServer;
    }

    public void setPrimaryVirtualServer(VirtualServer primaryVirtualServer) {
        this.primaryVirtualServer = primaryVirtualServer;
    }
}
