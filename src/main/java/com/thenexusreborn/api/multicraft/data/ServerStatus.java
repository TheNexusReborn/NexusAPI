package com.thenexusreborn.api.multicraft.data;

import java.util.*;

public class ServerStatus implements MulticraftObject {
    public int serverId, onlinePlayers, maxPlayers;
    public String status;
    public Set<PlayerStatus> players = new HashSet<>();
}
