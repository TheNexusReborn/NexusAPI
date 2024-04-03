package com.thenexusreborn.api.server;

public abstract non-sealed class VirtualServer extends NexusServer {
    
    protected InstanceServer parentServer;
    
    public VirtualServer(InstanceServer parent, String name, String mode, int maxPlayers) {
        super(name, ServerType.VIRTUAL, mode, maxPlayers);
        this.parentServer = parent;
    }

    public VirtualServer(String name, String mode, int maxPlayers) {
        super(name, ServerType.VIRTUAL, mode, maxPlayers);
    }

    public InstanceServer getParentServer() {
        return parentServer;
    }

    public void setParentServer(InstanceServer parentServer) {
        this.parentServer = parentServer;
    }
}