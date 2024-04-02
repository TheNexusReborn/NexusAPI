package com.thenexusreborn.api.server;

import com.thenexusreborn.api.player.NexusPlayer;

public abstract sealed class NexusServer permits ProxyServer, InstanceServer, VirtualServer {
    protected String name; //Server Name
    protected ServerType type; //Server Type
    protected String mode; //Things like hub, sg or other gamemodes
    protected String status; //Status like online, offline, error etc...
    protected String state; //Format determined by plugin, different information about the server

    public NexusServer(String name, ServerType type, String mode) {
        this.name = name;
        this.type = type;
        this.mode = mode;
    }
    
    public abstract void join(NexusPlayer player);
    public abstract void quit(NexusPlayer player);
    
    public abstract void onStart();
    public abstract void onStop();

    public String getName() {
        return name;
    }

    public ServerType getType() {
        return type;
    }

    public String getMode() {
        return mode;
    }

    public String getStatus() {
        return status;
    }

    public String getState() {
        return state;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setState(String state) {
        this.state = state;
    }
}