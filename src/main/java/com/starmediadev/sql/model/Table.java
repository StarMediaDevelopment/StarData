package com.starmediadev.sql.model;

import com.starmediadev.utils.collection.IncrementalMap;

import java.util.*;

public class Table {
    protected String name, recordName;
    protected IncrementalMap<Column> columns = new IncrementalMap<>();
    protected List<String> databases = new ArrayList<>();

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, Collection<Column> columns) {
        this(name);
        for (Column column : columns) {
            this.columns.add(column);
        }
    }
    
    public void addDatabases(String... databases) {
        this.databases.addAll(Arrays.asList(databases));
    }

    public List<String> getDatabases() {
        return databases;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public String generateCreationStatement(String database) {
        //TODO Column sorting will also need to add something to ColumnInfo annotation as well
        StringBuilder colBuilder = new StringBuilder();
        Iterator<Column> columnIterator = columns.values().iterator();
        while (columnIterator.hasNext()) {
            Column column = columnIterator.next();
            colBuilder.append(column.getCreationString());
            if (columnIterator.hasNext()) {
                colBuilder.append(",");
            }
        }

        return Statements.CREATE_TABLE.replace("{table}", name).replace("{columns}", colBuilder.toString()).replace("{database}", database);
    }

    public void addColumns(Column... columns) {
        for (Column column : columns) {
            this.columns.add(column);
        }
    }

    public String getName() {
        return name;
    }

    public Collection<Column> getColumns() {
        return columns.values();
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public Column getColumn(String columnName) {
        for (Column column : columns.values()) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }
}