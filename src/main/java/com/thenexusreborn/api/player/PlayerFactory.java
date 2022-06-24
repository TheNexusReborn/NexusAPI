package com.thenexusreborn.api.player;

import com.thenexusreborn.api.tags.Tag;

import java.util.*;

public abstract class PlayerFactory {
    public abstract NexusPlayer createPlayer(UUID uuid, Map<Rank, Long> ranks, long firstJoined, long lastLogin, long lastLogout, String name, Tag tag, Set<String> unlockedTags, boolean prealpha, boolean alpha, boolean beta);
    public abstract NexusPlayer createPlayer(UUID uniqueId, String name);
}
