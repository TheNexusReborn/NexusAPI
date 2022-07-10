package com.thenexusreborn.api.data.objects;

import java.sql.*;
import java.util.*;

public class Row {
    private Map<String, Object> data = new HashMap<>();
    
    public Row(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Object object = rs.getObject(i);
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