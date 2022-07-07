package com.thenexusreborn.api.data.objects;

import com.thenexusreborn.api.NexusAPI;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Database {
    public static final String URL = "jdbc:mysql://%s/%s?user=%s&password=%s";
    private final String name, host, user, password;
    private Set<Table> tables = new HashSet<>();
    
    public Database(String name, String host, String user, String password) {
        this.name = name;
        this.host = host;
        this.user = user;
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void addTable(Table table) {
        this.tables.add(table);
    }
    
    public Set<Table> getTables() {
        return tables;
    }
    
    public void push(Object object) {
        Class<?> clazz = object.getClass();
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to push data to the database without a registered table.",
                    "Class Name: " + clazz.getName(),
                    "Database Name: " + this.host + "/" + this.name);
            return;
        }
        
        Map<String, SqlCodec<?>> codecInstances = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        long id = 0; //This will hold an int as well
        Field primaryField = null;
        Column primaryColumn = null;
        for (Column column : table.getColumns()) {
            Field field = null;
            try {
                field = clazz.getDeclaredField(column.getName());
                field.setAccessible(true);
                Object value = field.get(object);
                if (column.getCodec() != null) {
                    if (!column.getCodec().equals(SqlCodec.class)) {
                        SqlCodec<?> codec;
                        if (codecInstances.containsKey(column.getCodec().getName())) {
                            codec = codecInstances.get(column.getCodec().getName());   
                        } else {
                            codec = column.getCodec().getConstructor().newInstance();
                            codecInstances.put(column.getCodec().getName(), codec);
                        }
                        value = codec.encode(value);
                    }
                }
                
                if (column.isPrimaryKey()) {
                    id = (long) value;
                    primaryField = field;
                    primaryColumn = column;
                }
                
                data.put(column.getName(), value);
            } catch (NoSuchFieldException e) {
                NexusAPI.logMessage(Level.WARNING, "Could not find a field for a column while saving to the database",
                        "Class: " + clazz.getName(),
                        "Column: " + column.getName());
                return;
            } catch (IllegalAccessException e) {
                NexusAPI.logMessage(Level.WARNING, "Could not access a field while saving to the database",
                        "Class: " + clazz.getName(),
                        "Field: " + field.getName());
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                NexusAPI.logMessage(Level.WARNING, "Could not instantiate codec class", 
                        "Codec Class: " + column.getCodec().getName(), 
                        "Object Class: " + clazz.getName(), 
                        "Field: " + field.getName());
            }
        }
        
        String sql;
        Iterator<Entry<String, Object>> iterator = data.entrySet().iterator();
        boolean getGeneratedKeys = false;
        if (id == 0) {
            StringBuilder cb = new StringBuilder(), vb = new StringBuilder();
            while (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                cb.append(entry.getKey());
                vb.append("'").append(entry.getValue()).append("'");
                if (iterator.hasNext()) {
                    cb.append(", ");
                    vb.append(", ");
                }
            }
            
            getGeneratedKeys = true;
            sql = "insert into " + table.getName() + " (" + cb +  ") values (" + vb +  ");";
        } else {
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                sb.append(entry.getKey()).append("='").append(entry.getValue()).append("'");
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sql = "update " + table.getName() + " set(" + sb + ") where " + primaryColumn.getName() + "='" + id + "';";
        }
        
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            if (getGeneratedKeys) {
                statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet generatedKeys = statement.getGeneratedKeys();
                generatedKeys.next();
                primaryField.set(object, generatedKeys.getObject(1));
            } else {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            NexusAPI.logMessage(Level.SEVERE, "Error while saving data to the database", "Exception: ");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            NexusAPI.logMessage(Level.SEVERE, "Could not set the primary field for generated keys", "Exception: ");
            e.printStackTrace();
        }
    }
    
    public Table getTable(String name) {
        for (Table table : new ArrayList<>(this.tables)) {
            if (table.getName().equalsIgnoreCase(name)) {
                return table;
            }
        }
        return null;
    }
    
    public Table getTable(Class<?> clazz) {
        for (Table table : new ArrayList<>(this.tables)) {
            if (table.getModelClass() == clazz) {
                return table;
            }
        }
        return null;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(String.format(URL, host, name, user, password));
    }
    
    public Row parseRow(String sql) throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            return new Row(rs);
        }
    }
    
    private boolean parsePreparedParmeters(PreparedStatement statement, Object... args) {
        if (args == null || args.length == 0) {
            NexusAPI.getApi().getLogger().severe("Invalid amount of paramenters for a prepared row, must be divisible by 2.");
            return false;
        }
        
        try {
            if (statement.getParameterMetaData().getParameterCount() != args.length) {
                NexusAPI.getApi().getLogger().severe("Not enough arguments for the amount of parameters in the statement.");
                return false;
            }
            
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i, args[i]); //Hopefully this will work, otherwise more things will be needed to make it work
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public Row parsePreparedRow(String sql, Object... args) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean value = parsePreparedParmeters(statement, args);
            if (value) {
                ResultSet rs = statement.executeQuery();
                return new Row(rs);
            }
        }
        
        return null;
    }
    
    public void execute(String sql) throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    public void executePrepared(String sql, Object... args) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean value = parsePreparedParmeters(statement, args);
            if (value) {
                statement.executeUpdate();
            }
        }
    }
    
    public List<Row> executeQuery(String sql) throws SQLException {
        List<Row> rows = new ArrayList<>();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                rows.add(new Row(resultSet));
            }
        }
        
        return rows;
    }
    
    public List<Row> executePreparedQuery(String sql, Object... args) throws SQLException {
        List<Row> rows = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean value = parsePreparedParmeters(statement, args);
            if (value) {
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    rows.add(new Row(resultSet));
                }
            }
        }
        
        return rows;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Database database = (Database) o;
        return Objects.equals(name, database.name) && Objects.equals(host, database.host);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, host);
    }
}
