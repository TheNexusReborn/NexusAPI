package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.network.netty.model.NexusPacket;

public abstract class NettyApp {
    
    protected final String host;
    protected final int port;
    
    public NettyApp(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public abstract void init() throws Exception;
    
    public void send(String origin, String action, String[] data) {
        send(new NexusPacket(origin, action, data));
    }
    
    public abstract void send(NexusPacket packet);
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
}
