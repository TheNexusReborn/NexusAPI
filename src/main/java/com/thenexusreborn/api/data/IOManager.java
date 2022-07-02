package com.thenexusreborn.api.data;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.*;
import com.thenexusreborn.api.data.objects.*;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class IOManager {
    private Set<Database> databases = new HashSet<>();
    
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
        for (Database db : this.databases) {
            if (db.getName().equalsIgnoreCase(database)) {
                rows.addAll(db.executeQuery(sql));
            }
        }
        return rows;
    }
    
    public List<Row> executePreparedQuery(String database, String sql, Object... args) throws SQLException {
        List<Row> rows = new ArrayList<>();
        for (Database db : this.databases) {
            if (db.getName().equalsIgnoreCase(database)) {
                rows.addAll(db.executePreparedQuery(sql, args));
            }
        }
        return rows;
    }
    
    public void addDatabase(Database database) {
        this.databases.add(database);
    }
    
    public Table parseTable(Class<?> modelClass) {
        String tableName;
        TableInfo tableInfo = modelClass.getAnnotation(TableInfo.class);
        if (tableInfo != null) {
            tableName = tableInfo.value();
        } else {
            tableName = modelClass.getSimpleName();
        }
        
        Table table = new Table(tableName, modelClass);
        Column primaryColumn = null;
        
        for (Field field : modelClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
            Primary primary = field.getAnnotation(Primary.class);
            String columnName = null;
            String type = null;
            boolean primaryKey = false, autoIncrement = false, notNull = false;
            Class<? extends SqlCodec<?>> codec = null;
            if (columnInfo != null) {
                columnName = columnInfo.name();
                type = columnInfo.type();
                primaryKey = columnInfo.primaryKey();
                autoIncrement = columnInfo.autoIncrement();
                notNull = columnInfo.notNull();
                codec = (Class<? extends SqlCodec<?>>) columnInfo.codec(); //TODO May not work 
            }
            
            if (primary != null) {
                autoIncrement = true;
                primaryKey = true;
            }
            
            if (columnName == null || columnName.equals("")) {
                columnName = field.getName();
            }
            
            if (type == null || type.equals("")) {
                Class<?> fieldType = field.getType();
                if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
                    type = "int";
                } else if (String.class.equals(fieldType) || char.class.equals(fieldType) || Character.class.equals(fieldType)) {
                    type = "varchar(1000);";
                } else if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
                    type = "bit";
                } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
                    type = "bigint";
                } else if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
                    type = "double";
                } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
                    type = "float";
                }
            }
            
            if (type == null || type.equals("")) {
                NexusAPI.getApi().getLogger().severe("There was a problem parsing the MySQL type for field " + field.getName() + " in class " + modelClass.getName());
                continue;
            }
            
            Column column = new Column(columnName, type, primaryKey, autoIncrement, notNull, codec);
            if (autoIncrement && primaryKey) {
                primaryColumn = column;
            }
            table.addColumn(column);
        }
        
        if (primaryColumn != null) {
            NexusAPI.logMessage(Level.SEVERE, "Could not find a primary column. This column must be a long or integer, and must be set to auto-increment and as the primary key. Or use the @Primary annotation");
            return null;
        }
        
        return table;
    }
    
    public Database getDatabase(String host, String name) {
        for (Database database : new ArrayList<>(this.databases)) {
            if (database.getHost().equalsIgnoreCase(host) && database.getName().equalsIgnoreCase(name)) {
                return database;
            }
        }
        return null;
    }
    
    public List<Database> getDatabasesByHost(String host) {
        List<Database> databases = new ArrayList<>();
        for (Database database : new ArrayList<>(this.databases)) {
            if (database.getHost().equalsIgnoreCase(host)) {
                databases.add(database);
            }
        }
        
        return databases;
    }
    
    public List<Database> getDatabasesByName(String name) {
        List<Database> databases = new ArrayList<>();
        for (Database database : new ArrayList<>(this.databases)) {
            if (database.getName().equalsIgnoreCase(name)) {
                databases.add(database);
            }
        }
        
        return databases;
    }
}
