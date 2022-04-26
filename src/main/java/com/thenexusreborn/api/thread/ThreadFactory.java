package com.thenexusreborn.api.thread;

public abstract class ThreadFactory {
    public abstract void runAsync(Runnable runnable);
    
    public abstract void runSync(Runnable runnable);
}
