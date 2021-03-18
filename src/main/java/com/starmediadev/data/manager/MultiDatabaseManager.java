package com.starmediadev.data.manager;

import com.starmediadev.data.model.IRecord;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.RecordRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.utils.collection.ListMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MultiDatabaseManager extends DatabaseManager {
    
    private Map<String, MysqlDatabase> databases = new HashMap<>();
    
    private ListMap<String, String> databaseToTableMap = new ListMap<>();

    public MultiDatabaseManager(Logger logger, RecordRegistry recordRegistry, TypeRegistry typeRegistry) {
        super(logger, recordRegistry, typeRegistry);
    }

    public MysqlDatabase createDatabase(SqlProperties properties) {
        MysqlDatabase database = new MysqlDatabase(logger, properties, typeRegistry);
        this.databases.put(database.getDatabaseName().toLowerCase(), database);
        return database;
    }

    public void saveRecord(IRecord record) {
        checkAndPushRecord(record);
    }

    public void saveRecords(IRecord... records) {
        for (IRecord record : records) {
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

    public <T extends IRecord> T getRecord(Class<T> recordType, String columnName, Object value) {
        for (MysqlDatabase database : this.databases.values()) {
            T record = database.getRecord(recordRegistry, recordType, columnName, value);
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    public <T extends IRecord> List<T> getRecords(Class<T> recordType, String columnName, Object value) {
        List<T> records = new ArrayList<>();
        for (MysqlDatabase database : this.databases.values()) {
            records.addAll(database.getRecords(recordRegistry, recordType, columnName, value));
        }
        return records;
    }

    private void checkAndPushRecord(IRecord record) {
        entryLoop:
        for (Map.Entry<String, List<String>> entry : databaseToTableMap.entrySet()) {
            for (String name : entry.getValue()) {
                if (record.getClass().isAssignableFrom(recordRegistry.getRecordByClassName(name))) {
                    MysqlDatabase database = databases.get(entry.getKey());
                    database.saveRecord(recordRegistry, record);
                    break entryLoop;
                }
            }
        }
    }
}
