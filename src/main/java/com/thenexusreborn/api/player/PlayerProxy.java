package com.thenexusreborn.api.player;

public interface PlayerProxy {
    void sendMessage(String message);
    
    boolean isOnline();
    
    String getName();
}