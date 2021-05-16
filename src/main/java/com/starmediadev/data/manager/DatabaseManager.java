package com.starmediadev.data.manager;

import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.model.IRecord;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.RecordRegistry;
import com.starmediadev.data.registries.TypeRegistry;

import java.util.List;
import java.util.logging.Logger;

public abstract class DatabaseManager {
    
    protected final RecordRegistry recordRegistry;
    protected final TypeRegistry typeRegistry;
    protected boolean setup;
    protected final Logger logger;

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
