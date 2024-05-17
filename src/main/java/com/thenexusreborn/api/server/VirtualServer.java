package com.thenexusreborn.api.server;

import com.stardevllc.starlib.observable.property.writable.ReadWriteObjectProperty;

import java.util.UUID;

public abstract non-sealed class VirtualServer extends NexusServer {
    
    protected final ReadWriteObjectProperty<InstanceServer> parentServer;
    
    public VirtualServer(InstanceServer parent, String name, String mode, int maxPlayers) {
        super(name, ServerType.VIRTUAL, mode, maxPlayers);
        this.parentServer = new ReadWriteObjectProperty<>(this, "parentServer", parent);
        
        this.parentServer.addListener((observableValue, oldValue, newValue) -> {
            if (oldValue != null) {
                for (UUID player : players) {
                    oldValue.getPlayers().remove(player);
                }
            }

            if (newValue != null) {
                for (UUID player : players) {
                    newValue.getPlayers().add(player);
                }
            }
        });
        
        this.players.addListener(change -> {
            if (parentServer.get() == null) {
                return;
            }
            
            if (change.wasAdded()) {
                parentServer.get().getPlayers().add(change.getElementAdded());
            } else if (change.wasRemoved()) {
                parentServer.get().getPlayers().remove(change.getElementRemoved());
            }
        });
    }

    public VirtualServer(String name, String mode, int maxPlayers) {
        this(null, name, mode, maxPlayers);
    }

    public InstanceServer getParentServer() {
        return parentServer.get();
    }

    public void setParentServer(InstanceServer parentServer) {
        this.parentServer.set(parentServer);
    }
}