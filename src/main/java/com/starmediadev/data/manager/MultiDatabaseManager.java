package com.starmediadev.data.manager;

import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.utils.collection.ListMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MultiDatabaseManager extends DatabaseManager {
    
    private final Map<String, MysqlDatabase> databases = new HashMap<>();
    
    private final ListMap<String, String> databaseToTableMap = new ListMap<>();

    public MultiDatabaseManager(Logger logger, DataObjectRegistry dataObjectRegistry, TypeRegistry typeRegistry) {
        super(logger, dataObjectRegistry, typeRegistry);
    }

    public MysqlDatabase createDatabase(SqlProperties properties) {
        MysqlDatabase database = new MysqlDatabase(logger, properties, typeRegistry);
        this.databases.put(database.getDatabaseName().toLowerCase(), database);
        return database;
    }

    public void saveRecord(IDataObject record) {
        checkAndPushRecord(record);
    }

    public void saveRecords(IDataObject... records) {
        for (IDataObject record : records) {
            checkAndPushRecord(record);
        }
    }

    public void registerTable(Table table) {
        for (MysqlDatabase database : databases.values()) {
            if (table.getDatabases().contains(database.getDatabaseName().toLowerCase())) {
                database.addTable(table);
                databaseToTableMap.add(database.getDatabaseName().toLowerCase(), table.getName());
            }
        }
        
        if (setup) {
            this.generateTables();
        }
    }

    public void generateTables() {
        this.databases.forEach((name, database) -> database.generateTables());
    }

    public <T extends IDataObject> T getRecord(Class<T> recordType, String columnName, Object value) {
        for (MysqlDatabase database : this.databases.values()) {
            T record = database.getAllMatchingData(dataObjectRegistry, recordType, columnName, value);
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    public <T extends IDataObject> List<T> getRecords(Class<T> recordType, String columnName, Object value) {
        List<T> records = new ArrayList<>();
        for (MysqlDatabase database : this.databases.values()) {
            records.addAll(database.getData(dataObjectRegistry, recordType, columnName, value));
        }
        return records;
    }

    private void checkAndPushRecord(IDataObject record) {
        entryLoop:
        for (Map.Entry<String, List<String>> entry : databaseToTableMap.entrySet()) {
            for (String name : entry.getValue()) {
                if (record.getClass().isAssignableFrom(dataObjectRegistry.getRecordByClassName(name))) {
                    MysqlDatabase database = databases.get(entry.getKey());
                    database.saveData(dataObjectRegistry, record);
                    break entryLoop;
                }
            }
        }
    }
}
