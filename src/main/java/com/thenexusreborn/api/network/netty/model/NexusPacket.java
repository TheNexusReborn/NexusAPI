package com.thenexusreborn.api.network.netty.model;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class NexusPacket {
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
    
    public static void encodeString(ByteBuf out, String string) {
        out.writeInt(string.length());
        for (char c : string.toCharArray()) {
            out.writeChar(c);
        }
    }
    
    public static String decodeString(ByteBuf in) {
        StringBuilder sb = new StringBuilder();
        int length = in.readInt();
        for (int i = 0; i < length; i++) {
            sb.append(in.readChar());
        }
        
        return sb.toString();
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
