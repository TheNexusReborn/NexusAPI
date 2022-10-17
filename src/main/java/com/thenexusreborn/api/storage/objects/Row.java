package com.thenexusreborn.api.storage.objects;

import com.thenexusreborn.api.NexusAPI;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class Row {
    private final Map<String, Object> data = new HashMap<>();
    
    public Row(ResultSet rs, Database database) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            
            Table table = database.getTable(metaData.getTableName(1));
            
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Column column = table.getColumn(columnName);
                if (column == null) {
                    continue;
                }
                
                Object object = null;
                
                if (column.getType().startsWith("int")) {
                    object = rs.getInt(i);
                } else if (column.getType().startsWith("bigint")) {
                    object = rs.getLong(i);
                } else if (column.getType().toLowerCase().startsWith("varchar")) {
                    object = rs.getString(i);
                } else if (column.getType().startsWith("double")) {
                    object = rs.getDouble(i);
                } else if (column.getType().startsWith("float")) {
                    object = rs.getFloat(i);
                } else {
                    NexusAPI.logMessage(Level.WARNING, "Unhandled MySQL Type", "Type: " + column.getType());
                }
                
                this.data.put(columnName, object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Object getObject(String key) {
        return data.get(key);
    }
    
    public String getString(String key) {
        return (String) data.get(key);
    }
    
    public int getInt(String key) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return 0;
    }
    
    public long getLong(String key) {
        Object value = data.get(key);
        if (value instanceof Long) {
            return (long) value;
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return 0;
    }
    
    public double getDouble(String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return 0;
    }
    
    public float getFloat(String key) {
        Object value = data.get(key);
        if (value instanceof Float) {
            return (float) value;
        } else if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        return 0;
    }
    
    public boolean getBoolean(String key) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Integer) {
            return ((int) value) == 1;
        } else if (value instanceof Long) {
            return ((long) value) == 1;
        }
        return false;
    }
    
    public <T> T get(String key, SqlCodec<T> codec) {
        Object value = data.get(key);
        return codec.decode((String) value);
    }
    
    public Map<String, Object> getData() {
        return data;
    }
}