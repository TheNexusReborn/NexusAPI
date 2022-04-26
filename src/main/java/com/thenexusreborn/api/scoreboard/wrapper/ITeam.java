package com.thenexusreborn.api.scoreboard.wrapper;

import java.util.Set;

public interface ITeam {
    void addEntry(String text);
    
    Set<String> getEntries();
    
    void unregister();
    
    String getName();
    
    void setPrefix(String prefix);
    
    void setSuffix(String suffix);
}
