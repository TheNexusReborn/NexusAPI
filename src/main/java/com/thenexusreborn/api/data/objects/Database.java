package com.thenexusreborn.api.data.objects;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.helper.ReflectionHelper;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Database {
    public static final String URL = "jdbc:mysql://%s/%s?user=%s&password=%s";
    private final String name, host, user, password;
    private final boolean primary;
    private final Set<Table> tables = new HashSet<>();
    
    private final LinkedList<Object> queue = new LinkedList<>();
    
    public Database(String name, String host, String user, String password, boolean primary) {
        this.name = name;
        this.host = host;
        this.user = user;
        this.password = password;
        this.primary = primary;
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
    
    public void registerClass(Class<?> clazz) {
        try {
            addTable(new Table(clazz));
        } catch (Exception e) {
            NexusAPI.logMessage(Level.SEVERE, "Error while registering a table", "Error Message: " + e.getMessage());
        }
    }
    
    public Set<Table> getTables() {
        return tables;
    }
    
    public <T> T parseObjectFromRow(Class<T> clazz, Row row) {
        Table table = getTable(clazz);
        T object;
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            object = constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            NexusAPI.logMessage(Level.SEVERE, "Could not create an instance of the class " + clazz.getName(), "Exception Type: " + e.getClass().getName(), "Exception Message: " + e.getMessage());
            return null;
        }
    
        for (String key : row.getData().keySet()) {
            Column column = table.getColumn(key);
            if (column == null) {
                NexusAPI.logMessage(Level.SEVERE, "Could not find a column for a table based on a MySQL query", "Column Name: " + key, "Class Name: " + clazz.getName(), "Table Name: " + table.getName());
                continue;
            }
        
            try {
                Object data = null;
                
                //TODO Have this detection using the codec system with default codecs
                Class<?> fieldType = column.getField().getType();
                if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
                    data = row.getInt(key);
                } else if (String.class.equals(fieldType) || char.class.equals(fieldType) || Character.class.equals(fieldType)) {
                    data = row.getString(key);
                } else if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
                    data = row.getBoolean(key);
                } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
                    data = row.getLong(key);
                } else if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
                    data = row.getDouble(key);
                } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
                    data = row.getFloat(key);
                } else if (UUID.class.equals(fieldType)) {
                    data = UUID.fromString(row.getString(key));
                } else if (Enum.class.isAssignableFrom(fieldType)) {
                    try {
                        Method valueOfMethod = ReflectionHelper.getClassMethod(fieldType, "valueOf", String.class);
                        data = valueOfMethod.invoke(null, row.getString(key));
                    } catch (Exception e) {
                        
                    }
                } else {
                    Class<? extends SqlCodec<?>> codec = column.getCodec();
                    try {
                        SqlCodec<?> codecInstance = codec.getDeclaredConstructor().newInstance();
                        data = codecInstance.decode(row.getString(key));
                    } catch (Exception e) {
                        if (column.getType().startsWith("varchar")) {
                            data = row.getString(key);
                        }
                    }
                }
    
                column.getField().setAccessible(true);
                column.getField().set(object, data);
            } catch (Exception e) {
                NexusAPI.logMessage(Level.SEVERE, "Could not set the value of a field for a column while loading from database.", "Class Name: " + clazz.getName(), "Field Name: " + column.getField().getName(), "Exception Class: " + e.getClass().getName(), "Exception Message: " + e.getMessage());
            }
        }
    
        Class<? extends ObjectHandler> handler = table.getHandler();
        if (handler != null) {
            try {
                Constructor<?> constructor = handler.getDeclaredConstructor(Object.class, Database.class, Table.class);
                ObjectHandler o = (ObjectHandler) constructor.newInstance(object, this, table);
                o.afterLoad();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    
        return object;
    }
    
    public <T> List<T> get(Class<T> clazz, String[] columns, Object[] values) throws SQLException {
        if (columns == null || values == null) {
            throw new IllegalArgumentException("Columns or Values are null");
        }
        
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Columns has a size of " + columns.length + " and values has a size of " + values.length + ". They must be equal.");
        }
        
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to get data from a table that does not exist.", "Database: " + this.host + "/" + this.name, "Class: " + clazz.getName());
            return null;
        }
        
        Map<Column, Object> tableColumns = new HashMap<>();
    
        for (int i = 0; i < columns.length; i++) {
            Column column = table.getColumn(columns[i]);
            if (column != null) {
                tableColumns.put(column, values[i]);
            }
        }
        
        List<String> whereConditions = new ArrayList<>();
        tableColumns.forEach((column, value) -> whereConditions.add(column.getName() + "='" + value.toString() + "'"));
        
        StringBuilder sb = new StringBuilder("select * from " + table.getName() + " where ");
        for (int i = 0; i < whereConditions.size(); i++) {
            sb.append(whereConditions.get(i));
            if (i < whereConditions.size() - 1) {
                sb.append(" and ");
            }
        }
        
        sb.append(";");
    
        List<Row> rows = executeQuery(sb.toString());
        
        List<T> objects = new ArrayList<>();
        for (Row row : rows) {
            objects.add(parseObjectFromRow(clazz, row));
        }
    
        return objects;
    }
    
    public <T> List<T> get(Class<T> clazz, String columnName, Object value) throws SQLException {
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to get data from a table that does not exist.", "Database: " + this.host + "/" + this.name, "Class: " + clazz.getName());
            return null;
        }
        
        Column column = table.getColumn(columnName);
        if (column == null) {
            return new ArrayList<>();
        }
        
        List<Row> rows = executeQuery("select * from " + table.getName() + " where " + column.getName() + "='" + value + "';");
    
        List<T> objects = new ArrayList<>();
        for (Row row : rows) {
            objects.add(parseObjectFromRow(clazz, row));
        }
        return objects;
    }
    
    public <T> List<T> get(Class<T> clazz) throws SQLException {
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to get data from a table that does not exist.", "Database: " + this.host + "/" + this.name, "Class: " + clazz.getName());
            return null;
        }
        
        List<T> objects = new ArrayList<>();
    
        List<Row> rows = executeQuery("select * from " + table.getName());
        for (Row row : rows) {
            objects.add(parseObjectFromRow(clazz, row));
        }
    
        return objects;
    }
    
    private PushInfo generateObjectPushInfo(Object object) {
        Class<?> clazz = object.getClass();
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to push data to the database without a registered table.",
                    "Class Name: " + clazz.getName(),
                    "Database Name: " + this.host + "/" + this.name);
            return null;
        }
    
        Class<? extends ObjectHandler> handler = table.getHandler();
        ObjectHandler objectHandler = null;
        if (handler != null) {
            try {
                Constructor<?> constructor = handler.getDeclaredConstructor(Object.class, Database.class, Table.class);
                objectHandler = (ObjectHandler) constructor.newInstance(object, this, table);
                objectHandler.beforeSave();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    
        Map<String, SqlCodec<?>> codecInstances = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        long id = 0;
        Field primaryField = null;
        Column primaryColumn = null;
        for (Column column : table.getColumns()) {
            Field field = column.getField();
            try {
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
            
                if (value instanceof String) {
                    String str = (String) value;
                    str = str.replace("\\", "\\\\");
                    str = str.replace("'", "\\'");
                    value = str;
                }
            
                if (column.isPrimaryKey()) {
                    id = (long) value;
                    primaryField = field;
                    primaryColumn = column;
                    continue;
                }
            
                data.put(column.getName(), value);
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
        if (id <= 0) {
            StringBuilder cb = new StringBuilder(), vb = new StringBuilder();
            while (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                cb.append("`").append(entry.getKey()).append("`");
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
                sb.append("`").append(entry.getKey()).append("`='").append(entry.getValue()).append("'");
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sql = "update " + table.getName() + " set " + sb + " where " + primaryColumn.getName() + "='" + id + "';";
        }
        
        return new PushInfo(sql, getGeneratedKeys, primaryField, objectHandler);
    }
    
    public void push(Object object) {
        PushInfo pushInfo = generateObjectPushInfo(object);
        boolean getGeneratedKeys = pushInfo.isGenerateKeys();
        String sql = pushInfo.getSql();
        Field primaryField = pushInfo.getPrimaryField();
        
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            if (getGeneratedKeys) {
                statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet generatedKeys = statement.getGeneratedKeys();
                generatedKeys.next();
                primaryField.set(object, generatedKeys.getLong(1));
            } else {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            NexusAPI.logMessage(Level.SEVERE, "Error while saving data to the database", "SQL: " + sql ,"Exception: ");
            e.printStackTrace();
        } catch (Exception e) {
            NexusAPI.logMessage(Level.SEVERE, "Could not set the primary field for generated keys", "Exception: ");
            e.printStackTrace();
        }
        
        ObjectHandler objectHandler = pushInfo.getObjectHandler();
        if (objectHandler != null) {
            objectHandler.afterSave();
        }
    }
    
    public void delete(Class<?> clazz, long id) {
        Table table = getTable(clazz);
        if (table == null) {
            NexusAPI.logMessage(Level.WARNING, "Tried to delete data to the database without a registered table.",
                    "Class Name: " + clazz.getName(),
                    "Database Name: " + this.host + "/" + this.name);
            return;
        }
        
        Column primaryColumn = null;
        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryColumn = column;
            }
        }
        
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("delete from " + table.getName() + " where " + primaryColumn.getName() + "='" + id + "';");
        } catch (SQLException e) {
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
            if (table.getModelClass().equals(clazz)) {
                return table;
            }
        }
        
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getTable(clazz.getSuperclass());
        }
        
        return null;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(String.format(URL, host, name, user, password));
    }
    
    public Row parseRow(String sql) throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            return new Row(rs, this);
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
                return new Row(rs, this);
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
                rows.add(new Row(resultSet, this));
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
                    rows.add(new Row(resultSet, this));
                }
            }
        }
        
        return rows;
    }
    
    public boolean isPrimary() {
        return primary;
    }
    
    public void queue(Object object) {
        this.queue.add(object);
    }
    
    public void flush() {
        NexusAPI.getApi().getLogger().info("Processing Database Queue...");
        if (!this.queue.isEmpty()) {
            try (Connection connection = getConnection()) {
                for (Object object : this.queue) {
                    PushInfo pushInfo = generateObjectPushInfo(object);
                    Statement statement = connection.createStatement();
                    if (pushInfo.isGenerateKeys()) {
                        statement.executeUpdate(pushInfo.getSql(), Statement.RETURN_GENERATED_KEYS);
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        generatedKeys.next();
                        pushInfo.getPrimaryField().set(object, generatedKeys.getLong(1));
                    } else {
                        statement.executeUpdate(pushInfo.getSql());
                    }
                    
                    if (pushInfo.getObjectHandler() != null) {
                        pushInfo.getObjectHandler().afterSave();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }
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
