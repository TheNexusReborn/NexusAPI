package com.thenexusreborn.api.data.objects;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;

public class Table implements Comparable<Table> {
    private final String name;
    private final Class<?> modelClass;
    private final Set<Column> columns = new TreeSet<>();
    
    public Table(Class<?> modelClass) {
        this.modelClass = modelClass;
        TableInfo tableInfo = modelClass.getAnnotation(TableInfo.class);
        if (tableInfo != null) {
            name = tableInfo.value();
        } else {
            name = modelClass.getSimpleName();
        }
    
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
            this.columns.add(column);
        }
    
        if (primaryColumn != null) {
            NexusAPI.logMessage(Level.SEVERE, "Could not find a primary column. This column must be a long or integer, and must be set to auto-increment and as the primary key. Or use the @Primary annotation");
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Class<?> getModelClass() {
        return modelClass;
    }
    
    public Set<Column> getColumns() {
        return new HashSet<>(columns);
    }
    
    public void addColumn(Column column) {
        this.columns.add(column);
    }
    
    public String generateCreationStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists");
        sb.append(getName()).append("(");
        getColumns().forEach(column -> {
            sb.append(column.getName()).append(" ").append(column.getType());
            if (column.isPrimaryKey()) {
                sb.append(" primary key");
            }
            
            if (column.isAutoIncrement()) {
                sb.append(" auto_increment");
            }
            
            if (column.isNotNull()) {
                sb.append(" not null");
            }
            
            sb.append(", ");
        });
        
        sb.delete(sb.length() - 2, sb.length() - 1);
        sb.append(");");
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
        Table table = (Table) o;
        return Objects.equals(name, table.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public int compareTo(Table o) {
        return this.name.compareTo(o.name);
    }
}
