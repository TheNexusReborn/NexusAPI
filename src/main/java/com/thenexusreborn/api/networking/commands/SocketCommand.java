package com.thenexusreborn.api.networking.commands;

public class SocketCommand {
    private final String name;
    private final String[] aliases;
    private final String description;
    
    private SocketCommandExecutor executor;

    public SocketCommand(String name, String[] aliases, String description) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public SocketCommandExecutor getExecutor() {
        return executor;
    }

    public Object setExecutor(SocketCommandExecutor executor) {
        this.executor = executor;
        return this;
    }
}
