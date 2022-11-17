package com.thenexusreborn.api.storage;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.storage.objects.*;
import com.thenexusreborn.api.registry.DatabaseRegistry;

import java.sql.*;
import java.util.*;

@SuppressWarnings("ClassCanBeRecord")
public class StorageManager {
    private final DatabaseRegistry registry;
    
    public StorageManager(DatabaseRegistry registry) {
        this.registry = registry;
    }
    
    public void execute(String database, String sql) throws SQLException {
        for (Database db : getDatabasesByName(database)) {
            db.execute(sql);
        }
    }
    
    public void executePrepared(String database, String sql, Object... args) throws SQLException {
        for (Database db : getDatabasesByName(database)) {
            db.executePrepared(sql, args);
        }
    }
    
    public List<Row> executeQuery(String database, String sql) throws SQLException {
        List<Row> rows = new ArrayList<>();
        for (Database db : this.registry.getObjects()) {
            if (db.getName().equalsIgnoreCase(database)) {
                rows.addAll(db.executeQuery(sql));
            }
        }
        return rows;
    }
    
    public List<Row> executePreparedQuery(String database, String sql, Object... args) throws SQLException {
        List<Row> rows = new ArrayList<>();
        for (Database db : this.registry.getObjects()) {
            if (db.getName().equalsIgnoreCase(database)) {
                rows.addAll(db.executePreparedQuery(sql, args));
            }
        }
        return rows;
    }
    
    public Database getDatabase(String host, String name) {
        for (Database database : new ArrayList<>(this.registry.getObjects())) {
            if (database.getHost().equalsIgnoreCase(host) && database.getName().equalsIgnoreCase(name)) {
                return database;
            }
        }
        return null;
    }
    
    public List<Database> getDatabasesByHost(String host) {
        List<Database> databases = new ArrayList<>();
        for (Database database : new ArrayList<>(this.registry.getObjects())) {
            if (database.getHost().equalsIgnoreCase(host)) {
                databases.add(database);
            }
        }
        
        return databases;
    }
    
    public List<Database> getDatabasesByName(String name) {
        List<Database> databases = new ArrayList<>();
        for (Database database : new ArrayList<>(this.registry.getObjects())) {
            if (database.getName().equalsIgnoreCase(name)) {
                databases.add(database);
            }
        }
        
        return databases;
    }
    
    public void setup() throws SQLException {
        for (Database database : this.registry.getObjects()) {
            for (Table table : database.getTables()) {
                String sql = table.generateCreationStatement();
                try {
                    database.execute(sql);
                } catch (SQLException e) {
                    NexusAPI.getApi().getLogger().severe(sql);
                    throw e;
                }
            }
        }
    }
    
    public DatabaseRegistry getRegistry() {
        return this.registry;
    }
}
