package com.thenexusreborn.api.registry;

import com.thenexusreborn.api.network.cmd.NetworkCommand;
import me.firestar311.starlib.api.Registry;

public class NetworkCommandRegistry extends Registry<NetworkCommand> {
    @Override
    public NetworkCommand get(String str) {
        for (NetworkCommand object : this.getRegisteredObjects().values()) {
            if (object.getName().equalsIgnoreCase(str)) {
                return object;
            }
        }
        return null;
    }
}
