package com.thenexusreborn.api.helper;

public final class MemoryHelper {
    public static double toKiloBytes(long bytes) {
        return bytes / 1024.0;
    }
    
    public static double toMegabytes(long bytes) {
        return toKiloBytes(bytes) / 1024;
    }
    
    public static double toGigabytes(long bytes) {
        return toMegabytes(bytes) / 1024;
    }
    
    public static double toTerabytes(long bytes) {
        return toGigabytes(bytes) / 1024;
    }
    
    public static double toPetabytes(long bytes) {
        return toTerabytes(bytes) / 1024;
    }
}
