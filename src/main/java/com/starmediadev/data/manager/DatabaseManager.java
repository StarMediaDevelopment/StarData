package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
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

    public static final String URL = "jdbc:mysql://{hostname}:{port}";
    
    protected final StarData starData;
    protected final DataObjectRegistry dataObjectRegistry;
    protected final TypeRegistry typeRegistry;
    protected boolean setup;
    protected final Logger logger;

    public DatabaseManager(StarData starData) {
        this.starData = starData;
        this.logger = starData.getLogger();
        this.dataObjectRegistry = starData.getDataObjectRegistry();
        this.typeRegistry = starData.getTypeRegistry();
    }
    
    public abstract MysqlDatabase setupDatabase(SqlProperties properties);
    public abstract void saveData(IDataObject record);
    public abstract void saveAllData(IDataObject... records);
    public abstract void registerTable(Table table, String... databases);
    public abstract void deleteData(IDataObject object);
    public void registerObjectAsTable(Class<? extends IDataObject> object, String... databases) {
        Table table = dataObjectRegistry.getTableByDataClass(object);
        if (table == null) {
            table = dataObjectRegistry.register(object);
        }
        registerTable(table, databases);
    }
    
    public void registerTypeHandler(DataTypeHandler<?> dataTypeHandler) {
        this.typeRegistry.register(dataTypeHandler);
    }
    
    public abstract void generate();
    public abstract <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value);
    public abstract <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value);

    public <T extends IDataObject> T getData(Class<T> recordType, String databaseName, String columnName, Object value) {
        return getData(recordType, columnName, value);
    }
    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String databaseName, String columnName, Object value) {
        return getAllData(recordType, columnName, value);
    }
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
