package com.thenexusreborn.api.migration;

import com.thenexusreborn.api.server.Version;

import java.util.Objects;

public abstract class Migrator implements Comparable<Migrator> {
    protected final Version targetVersion;
    protected final Version requiredVersion;
    
    public Migrator(Version targetVersion, Version requiredVersion) {
        this.targetVersion = targetVersion;
        this.requiredVersion = requiredVersion;
    }
    
    public abstract boolean migrate();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Migrator migrator = (Migrator) o;
        return Objects.equals(targetVersion, migrator.targetVersion) && Objects.equals(requiredVersion, migrator.requiredVersion);
    }
    
    public Version getTargetVersion() {
        return targetVersion;
    }
    
    public Version getRequiredVersion() {
        return requiredVersion;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(targetVersion, requiredVersion);
    }
    
    @Override
    public int compareTo(Migrator o) {
        
        
        return 0;
    }
}