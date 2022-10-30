package com.thenexusreborn.api.storage.objects;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.storage.annotations.*;
import com.thenexusreborn.api.helper.ReflectionHelper;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;

public class Table implements Comparable<Table> {
    private String name;
    private final Class<?> modelClass;
    private final Set<Column> columns = new TreeSet<>();
    private Class<? extends ObjectHandler> handler;
    
    public Table(Class<?> modelClass) {
        this.modelClass = modelClass;
    
        try {
            modelClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find default constructor for class " + modelClass.getName());
        }
    
        name = determineTableName(modelClass);

        if (name == null) {
            name = modelClass.getSimpleName().toLowerCase();
        }
    
        Column primaryColumn = null;
    
        for (Field field : ReflectionHelper.getClassFields(modelClass)) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
    
            if (field.isAnnotationPresent(ColumnIgnored.class)) {
                continue;
            }
            
            if (Modifier.isFinal(field.getModifiers())) {
                NexusAPI.logMessage(Level.WARNING, "Field in a table class is final. These will be ignored", "Class Name: " + modelClass.getName(), "Field Name: " + field.getName());
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
            NexusAPI.logMessage(Level.SEVERE, "Could not find a primary column. This column must be a long, and must be set to auto-increment and as the primary key. Or use the @Primary annotation", "Model Class: " + modelClass.getName());
        }
    }

    private static String determineTableName(Class<?> clazz) {
        if (clazz.equals(Object.class)) {
            return null;
        }

        TableInfo tableInfo = clazz.getAnnotation(TableInfo.class);
        if (tableInfo == null) {
            return determineTableName(clazz.getSuperclass());
        }

        if (tableInfo.value() == null || tableInfo.value().equals("")) {
            return determineTableName(clazz.getSuperclass());
        }

        return tableInfo.value();
    }

    public Class<? extends ObjectHandler> getHandler() {
        return handler;
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
            sb.append("`").append(column.getName()).append("`").append(" ").append(column.getType());
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
    
    public Column getColumn(String columnName) {
        for (Column column : this.columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }
}
