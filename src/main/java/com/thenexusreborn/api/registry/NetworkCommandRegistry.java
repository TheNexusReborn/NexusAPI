package com.thenexusreborn.api.registry;

import com.starmediadev.starlib.util.Registry;
import com.thenexusreborn.api.network.cmd.NetworkCommand;

public class NetworkCommandRegistry extends Registry<NetworkCommand> {
    @Override
    public NetworkCommand get(String str) {
        for (NetworkCommand object : this.getObjects()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
