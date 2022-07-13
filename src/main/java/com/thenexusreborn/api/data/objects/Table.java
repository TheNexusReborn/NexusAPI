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
    
        try {
            Constructor<?> defaultConstructor = modelClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find default constructor for class " + modelClass.getName());
        }
    
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
    
            Column column = new Column(modelClass, field);
        
            if (column.getType() == null || column.getType().equals("")) {
                NexusAPI.getApi().getLogger().severe("There was a problem parsing the MySQL type for field " + field.getName() + " in class " + modelClass.getName());
                continue;
            }
        
            if (column.isAutoIncrement() && column.isPrimaryKey()) {
                primaryColumn = column;
            }
            this.columns.add(column);
        }
    
        if (primaryColumn == null) {
            NexusAPI.logMessage(Level.SEVERE, "Could not find a primary column. This column must be a long, and must be set to auto-increment and as the primary key. Or use the @Primary annotation");
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
        sb.append("create table if not exists ");
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
