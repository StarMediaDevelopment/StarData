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

    public MysqlDatabase setupDatabase(SqlProperties properties) {
        database = new MysqlDatabase(logger, properties, typeRegistry);
        return database;
    }

    public void saveData(IDataObject record) {
        database.saveData(dataObjectRegistry, record);
    }

    public void saveAllData(IDataObject... records) {
        database.saveAllData(dataObjectRegistry, records);
    }

    public void registerTable(Table table) {
        database.addTable(table);
        if (this.setup) {
            generate();
        }
    }

    public void deleteData(IDataObject object) {
        this.database.deleteData(dataObjectRegistry, object);
    }

    public void generate() {
        database.generateTables();
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        return database.getData(this.dataObjectRegistry, recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        return database.getAllData(dataObjectRegistry, recordType, columnName, value);
    }
}