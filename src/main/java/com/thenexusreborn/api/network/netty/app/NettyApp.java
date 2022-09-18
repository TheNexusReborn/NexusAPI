package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.channel.EventLoopGroup;

import java.util.*;

public abstract class NettyApp {
    
    protected final String host;
    protected final int port;
    
    protected final List<EventLoopGroup> groups = new ArrayList<>();
    
    public NettyApp(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public abstract void init();
    
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
    
    public void close() {
        for (EventLoopGroup group : groups) {
            group.shutdownGracefully();
        }
    }
}
