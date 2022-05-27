package com.thenexusreborn.api.network.cmd;

public interface NetworkCommandExecutor {
    void handle(NetworkCommand cmd, String origin, String[] args);
}
