package com.thenexusreborn.api.data.objects;

import java.util.*;

public class Table implements Comparable<Table> {
    private final String name;
    private final Class<?> modelClass;
    private final Set<Column> columns = new TreeSet<>();
    
    public Table(String name, Class<?> modelClass) {
        this.name = name.toLowerCase();
        this.modelClass = modelClass;
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
    
    public String generateTableCreationStatement() {
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
