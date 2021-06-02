package com.starmediadev.data.manager;

import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import com.starmediadev.data.model.MysqlDatabase;

import java.util.List;
import java.util.logging.Logger;

public class SingleDatabaseManager extends DatabaseManager {

    private MysqlDatabase database;

    public SingleDatabaseManager(Logger logger, DataObjectRegistry dataObjectRegistry, TypeRegistry typeRegistry) {
        super(logger, dataObjectRegistry, typeRegistry);
    }

    public MysqlDatabase createDatabase(SqlProperties properties) {
        database = new MysqlDatabase(logger, properties, typeRegistry);
        return database;
    }

    public void saveRecord(IDataObject record) {
        database.saveData(dataObjectRegistry, record);
    }

    public void saveRecords(IDataObject... records) {
        database.saveAllData(dataObjectRegistry, records);
    }

    public void registerTable(Table table) {
        database.addTable(table);
        if (this.setup) {
            generateTables();
        }
    }

    public void generateTables() {
        database.generateTables();
    }

    public <T extends IDataObject> T getRecord(Class<T> recordType, String columnName, Object value) {
        return database.getAllMatchingData(this.dataObjectRegistry, recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getRecords(Class<T> recordType, String columnName, Object value) {
        return database.getData(dataObjectRegistry, recordType, columnName, value);
    }
}