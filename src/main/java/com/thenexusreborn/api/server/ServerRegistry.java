package com.thenexusreborn.api.server;

import com.stardevllc.starlib.registry.StringRegistry;

public class ServerRegistry<T extends NexusServer> extends StringRegistry<T> {
    public ServerRegistry() {
        super(string -> string.toLowerCase().replace(" ", "_"), NexusServer::getName);
    }
}