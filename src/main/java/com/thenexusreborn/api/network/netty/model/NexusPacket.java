package com.thenexusreborn.api.network.netty.model;

import java.nio.charset.*;
import java.util.Arrays;

public record NexusPacket(String origin, String action, String[] data) {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    
    @Override
    public String toString() {
        return "NexusPacket{" +
                "origin='" + origin + '\'' +
                ", action='" + action + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
