package com.thenexusreborn.api.server;

import com.stardevllc.starlib.observable.collections.ObservableCollections;
import com.stardevllc.starlib.observable.collections.set.ObservableSet;
import com.stardevllc.starlib.observable.property.readonly.ReadOnlyIntegerProperty;
import com.stardevllc.starlib.observable.property.readonly.ReadOnlyObjectProperty;
import com.stardevllc.starlib.observable.property.readonly.ReadOnlyStringProperty;
import com.stardevllc.starlib.observable.property.writable.ReadWriteStringProperty;
import com.thenexusreborn.api.player.NexusPlayer;

import java.util.UUID;

public abstract sealed class NexusServer permits ProxyServer, InstanceServer, VirtualServer {
    protected final ReadOnlyStringProperty name; //Server Name
    protected final ReadOnlyObjectProperty<ServerType> type; //Server Type, effectively final
    protected final ReadOnlyStringProperty mode; //Things like hub, sg or other gamemodes, effectively final
    protected final ReadWriteStringProperty status; //Status like online, offline, error etc...
    protected final ReadWriteStringProperty state; //Format determined by plugin, different information about the server
    
    protected final ReadOnlyIntegerProperty maxPlayers; //Maximum of players allowed.
    protected final ObservableSet<UUID> players; //Players currently in this server.

    public NexusServer(String name, ServerType type, String mode, int maxPlayers) {
        this.name = new ReadOnlyStringProperty(this, "name", name);
        this.type = new ReadOnlyObjectProperty<>(this, "type", type);
        this.mode = new ReadOnlyStringProperty(this, "mode", mode);
        this.status = new ReadWriteStringProperty(this, "status", "");
        this.state = new ReadWriteStringProperty(this, "state", "");
        this.maxPlayers = new ReadOnlyIntegerProperty(this, "maxPlayers", maxPlayers);
        this.players = ObservableCollections.observableSet();
    }
    
    public abstract void join(NexusPlayer player);
    public abstract void quit(NexusPlayer player);
    
    public abstract void onStart();
    public abstract void onStop();
    
    public boolean recalculateVisibility(UUID player, UUID otherPlayer) {
        return false;
    }

    public String getName() {
        return name.get();
    }

    public ServerType getType() {
        return type.get();
    }

    public String getMode() {
        return mode.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getState() {
        return state.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setState(String state) {
        this.state.set(state);
    }

    public int getMaxPlayers() {
        return maxPlayers.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return this.name;
    }
    
    public ReadOnlyObjectProperty<ServerType> typeProperty() {
        return this.type;
    }
    
    public ReadWriteStringProperty statusProperty() {
        return this.status;
    }
    
    public ReadWriteStringProperty stateProperty() {
        return state;
    }
    
    public ReadOnlyIntegerProperty maxPlayersProperty() {
        return maxPlayers;
    }
    
    public ObservableSet<UUID> getPlayers() {
        return players;
    }
}