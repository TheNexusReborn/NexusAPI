package com.thenexusreborn.api.server;

import com.thenexusreborn.api.NexusAPI;

import java.util.Objects;
import java.util.logging.Level;

public class Version implements Comparable<Version> {
    private int major, minor, patch;
    private Stage stage;
    
    public Version(int major, int minor, int patch, Stage stage) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.stage = stage;
    }
    
    public Version(String raw) {
        if (raw == null || raw.equals("")) {
            return;
        }
        String[] mainSplit = raw.split("-");
        if (mainSplit == null || mainSplit.length != 2) {
            NexusAPI.logMessage(Level.SEVERE, "Incorrect Version String " + raw);
            return;
        }
    
        try {
            this.stage = Stage.valueOf(mainSplit[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            NexusAPI.logMessage(Level.SEVERE, "Incorrect Stage Argument: " + mainSplit[1]);
            return;
        }
        
        String[] rawNumberSplit = mainSplit[0].split("\\.");
        if (rawNumberSplit == null || rawNumberSplit.length > 3) {
            NexusAPI.logMessage(Level.SEVERE, "Incorrect Numeric Version: " + mainSplit[0]);
            return;
        }
        
        if (rawNumberSplit.length >= 1) {
            this.major = Integer.parseInt(rawNumberSplit[0]);
        }
        
        if (rawNumberSplit.length >= 2) {
            this.minor = Integer.parseInt(rawNumberSplit[1]);
        }
        
        if (rawNumberSplit.length == 3) {
            this.patch = Integer.parseInt(rawNumberSplit[2]);
        }
    }
    
    public int getMajor() {
        return major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public int getPatch() {
        return patch;
    }
    
    public Stage getStage() {
        return stage;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.major).append(".").append(this.minor);
        if (this.patch > 0) {
            sb.append(".").append(this.patch);
        }
        sb.append("-").append(this.stage.name());
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch && stage == version.stage;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, stage);
    }
    
    @Override
    public int compareTo(Version o) {
        if (o == null) {
            return 1;
        }
        
        if (this.stage != o.stage) {
            return -Integer.compare(this.stage.ordinal(), o.stage.ordinal());
        }
        
        if (this.major != o.major) {
            return Integer.compare(this.major, o.major);
        }
        
        if (this.minor != o.minor) {
            return Integer.compare(this.minor, o.minor);
        }
        
        if (this.patch != o.patch) {
            return Integer.compare(this.patch, o.patch);
        }
        
        return -1;
    }
    
    public enum Stage {
        RELEASE, BETA, ALPHA, EXPERIMENTAL, SNAPSHOT
    }
}
