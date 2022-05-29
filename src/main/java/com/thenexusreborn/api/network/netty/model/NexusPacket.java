package com.thenexusreborn.api.network.netty.model;

import java.nio.charset.*;
import java.util.Arrays;

public class NexusPacket {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    
    private String origin;
    private String action;
    private String[] data;
    
    public NexusPacket(String origin, String action, String[] data) {
        this.origin = origin;
        this.action = action;
        this.data = data;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public String getAction() {
        return action;
    }
    
    public String[] getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return "NexusPacket{" +
                "origin='" + origin + '\'' +
                ", action='" + action + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
