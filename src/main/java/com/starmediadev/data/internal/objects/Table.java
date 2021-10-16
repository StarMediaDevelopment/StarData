package com.starmediadev.data.internal.objects;

import com.starmediadev.utils.collection.IncrementalMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Table {
    protected final String name;
    protected String recordName;
    protected final IncrementalMap<Column> columns = new IncrementalMap<>();
    protected final Set<String> databases = new HashSet<>();

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, Collection<Column> columns) {
        this(name);
        for (Column column : columns) {
            this.columns.add(column);
        }
    }

    public void addDatabase(String database) {
        this.databases.add(database);
    }

    public Set<String> getDatabases() {
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