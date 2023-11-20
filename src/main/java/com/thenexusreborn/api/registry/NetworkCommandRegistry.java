package com.thenexusreborn.api.registry;

import com.stardevllc.starlib.registry.StringRegistry;
import com.thenexusreborn.api.network.cmd.NetworkCommand;

public class NetworkCommandRegistry extends StringRegistry<NetworkCommand> {
    @Override
    public NetworkCommand get(String str) {
        for (NetworkCommand object : this.getObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
