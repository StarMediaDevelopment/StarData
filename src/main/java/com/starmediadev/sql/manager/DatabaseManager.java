package com.starmediadev.sql.manager;

import com.starmediadev.sql.handlers.DataTypeHandler;
import com.starmediadev.sql.model.IRecord;
import com.starmediadev.sql.model.MysqlDatabase;
import com.starmediadev.sql.model.Table;
import com.starmediadev.sql.properties.SqlProperties;
import com.starmediadev.sql.registries.RecordRegistry;
import com.starmediadev.sql.registries.TypeRegistry;

import java.util.List;
import java.util.logging.Logger;

public abstract class DatabaseManager {
    
    protected RecordRegistry recordRegistry;
    protected TypeRegistry typeRegistry;
    protected boolean setup;
    protected Logger logger;

    public DatabaseManager(Logger logger, RecordRegistry recordRegistry, TypeRegistry typeRegistry) {
        this.logger = logger;
        this.recordRegistry = recordRegistry;
        this.typeRegistry = typeRegistry;
    }
    
    public abstract MysqlDatabase createDatabase(SqlProperties properties);
    public abstract void saveRecord(IRecord record);
    public abstract void saveRecords(IRecord... records);
    public abstract void registerTable(Table table);
    public void registerRecordAsTable(Class<? extends IRecord> record) {
        Table table = recordRegistry.getTableByRecordClass(record);
        if (table == null) {
            table = recordRegistry.register(record);
        }
        registerTable(table);
    }
    
    public void registerTypeHandler(DataTypeHandler<?> dataTypeHandler) {
        this.typeRegistry.register(dataTypeHandler);
    }
    
    public abstract void generateTables();
    public abstract <T extends IRecord> T getRecord(Class<T> recordType, String columnName, Object value);
    public abstract <T extends IRecord> List<T> getRecords(Class<T> recordType, String columnName, Object value);
    public void setup() {
        generateTables();
        this.setup = true;
    }

    public RecordRegistry getRecordRegistry() {
        return recordRegistry;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
