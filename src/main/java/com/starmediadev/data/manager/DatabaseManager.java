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
    
    public abstract MysqlDatabase setupDatabase(SqlProperties properties);
    public abstract void saveData(IDataObject record);
    public abstract void saveAllData(IDataObject... records);
    public abstract void registerTable(Table table);
    public abstract void deleteData(IDataObject object);
    public void registerObjectAsTable(Class<? extends IDataObject> object) {
        Table table = dataObjectRegistry.getTableByDataClass(object);
        if (table == null) {
            table = dataObjectRegistry.register(object);
        }
        registerTable(table);
    }
    
    public void registerTypeHandler(DataTypeHandler<?> dataTypeHandler) {
        this.typeRegistry.register(dataTypeHandler);
    }
    
    public abstract void generate();
    public abstract <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value);
    public abstract <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value);
    public void setup() {
        generate();
        this.setup = true;
    }

    public DataObjectRegistry getDataObjectRegistry() {
        return dataObjectRegistry;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
