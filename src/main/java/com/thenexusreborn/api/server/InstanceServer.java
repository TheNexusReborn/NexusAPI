package com.thenexusreborn.api.server;

import com.stardevllc.beans.property.ObjectProperty;

public abstract non-sealed class InstanceServer extends NexusServer {

    protected final ObjectProperty<VirtualServer> primaryVirtualServer;
    private final ServerRegistry<VirtualServer> childServers = new ServerRegistry<>();
    
    public InstanceServer(String name, String mode, int maxPlayers) {
        super(name, ServerType.INSTANCE, mode, maxPlayers);
        this.primaryVirtualServer = new ObjectProperty<>(this, "primaryVirtualServer", null);
    }

    public ServerRegistry<VirtualServer> getChildServers() {
        return childServers;
    }

    public VirtualServer getPrimaryVirtualServer() {
        return primaryVirtualServer.get();
    }

    public void setPrimaryVirtualServer(VirtualServer primaryVirtualServer) {
        this.primaryVirtualServer.set(primaryVirtualServer);
    }
}
