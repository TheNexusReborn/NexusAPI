package com.thenexusreborn.api.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface NexusProfile {
    long getLastLogout();

    void setLastLogout(long lastLogout);

    boolean isOnline();

    void setOnline(boolean online);

    boolean isVanish();

    void setVanish(boolean vanish);

    boolean isIncognito();

    void setIncognito(boolean incognito);

    String getServer();

    void setServer(String server);

    long getId();

    void setName(String name);

    UUID getUniqueId();

    String getName();

    Set<IPEntry> getIpHistory();

    NexusPlayer loadFully();

    Map<Rank, Long> getRanks();

    Rank getRank();

    void addRank(Rank rank, long expire);

    void removeRank(Rank rank);

    void setRank(Rank rank, long expire);

    void setFly(boolean value);

    boolean isFly();

    Set<String> getUnlockedTags();

    void unlockTag(String tag);

    void lockTag(String tag);

    boolean isTagUnlocked(String tag);

    void addCredits(int credits);
    
    boolean isPrivateAlpha();
    
    void setPrivateAlpha(boolean value);
}
