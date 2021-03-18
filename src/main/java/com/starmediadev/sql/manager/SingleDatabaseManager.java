package com.starmediadev.sql.manager;

import com.starmediadev.sql.model.IRecord;
import com.starmediadev.sql.model.Table;
import com.starmediadev.sql.properties.SqlProperties;
import com.starmediadev.sql.registries.RecordRegistry;
import com.starmediadev.sql.registries.TypeRegistry;
import com.starmediadev.sql.model.MysqlDatabase;

import java.util.List;
import java.util.logging.Logger;

public class SingleDatabaseManager extends DatabaseManager {

    private MysqlDatabase database;

    public SingleDatabaseManager(Logger logger, RecordRegistry recordRegistry, TypeRegistry typeRegistry) {
        super(logger, recordRegistry, typeRegistry);
    }

    public MysqlDatabase createDatabase(SqlProperties properties) {
        database = new MysqlDatabase(logger, properties, typeRegistry);
        return database;
    }

    public void saveRecord(IRecord record) {
        database.saveRecord(recordRegistry, record);
    }

    public void saveRecords(IRecord... records) {
        database.saveRecords(recordRegistry, records);
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

    public <T extends IRecord> T getRecord(Class<T> recordType, String columnName, Object value) {
        return database.getRecord(this.recordRegistry, recordType, columnName, value);
    }

    public <T extends IRecord> List<T> getRecords(Class<T> recordType, String columnName, Object value) {
        return database.getRecords(recordRegistry, recordType, columnName, value);
    }
}