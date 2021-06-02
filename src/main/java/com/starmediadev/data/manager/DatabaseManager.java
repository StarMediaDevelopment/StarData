package com.starmediadev.data.manager;

import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;

import java.util.List;
import java.util.logging.Logger;

public abstract class DatabaseManager {
    
    protected final DataObjectRegistry dataObjectRegistry;
    protected final TypeRegistry typeRegistry;
    protected boolean setup;
    protected final Logger logger;

    public DatabaseManager(Logger logger, DataObjectRegistry dataObjectRegistry, TypeRegistry typeRegistry) {
        this.logger = logger;
        this.dataObjectRegistry = dataObjectRegistry;
        this.typeRegistry = typeRegistry;
    }
    
    public abstract MysqlDatabase createDatabase(SqlProperties properties);
    public abstract void saveRecord(IDataObject record);
    public abstract void saveRecords(IDataObject... records);
    public abstract void registerTable(Table table);
    public void registerRecordAsTable(Class<? extends IDataObject> record) {
        Table table = dataObjectRegistry.getTableByRecordClass(record);
        if (table == null) {
            table = dataObjectRegistry.register(record);
        }
        registerTable(table);
    }
    
    public void registerTypeHandler(DataTypeHandler<?> dataTypeHandler) {
        this.typeRegistry.register(dataTypeHandler);
    }
    
    public abstract void generateTables();
    public abstract <T extends IDataObject> T getRecord(Class<T> recordType, String columnName, Object value);
    public abstract <T extends IDataObject> List<T> getRecords(Class<T> recordType, String columnName, Object value);
    public void setup() {
        generateTables();
        this.setup = true;
    }

    public DataObjectRegistry getRecordRegistry() {
        return dataObjectRegistry;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
