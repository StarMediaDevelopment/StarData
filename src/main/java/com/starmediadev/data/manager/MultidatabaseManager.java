package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.MysqlDataSource;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultidatabaseManager extends DatabaseManager {
    
    private Map<String, MysqlDatabase> databases = new HashMap<>();
    private Map<String, MysqlDataSource> dataSources = new HashMap<>();
    
    public MultidatabaseManager(StarData starData) {
        super(starData);
    }

    public MysqlDatabase setupDatabase(SqlProperties properties) {
        if (!dataSources.containsKey(properties.getHost().toLowerCase())) {
            this.dataSources.put(properties.getHost().toLowerCase(), new MysqlDataSource(URL.replace("{host}", properties.getHost()).replace("port", properties.getPort() + ""), properties.getUsername(), properties.getPassword()));
        }
        
        if (this.databases.containsKey(properties.getDatabase().toLowerCase())) {
            throw new RuntimeException("Database " + properties.getDatabase() + " already exists.");
        }
        
        MysqlDatabase mysqlDatabase = new MysqlDatabase(starData, properties);
        this.databases.put(mysqlDatabase.getDatabaseName(), mysqlDatabase);
        return mysqlDatabase;
    }
    
    public void saveData(IDataObject record) {
        Pair<MysqlDatabase, MysqlDataSource> result = getDatabaseAndSource(record);
        result.getValue1().saveData(result.getValue2(), record);
    }

    public void saveAllData(IDataObject... records) {
        if (records != null) {
            for (IDataObject record : records) {
                saveData(record);
            }
        }
    }

    public void registerTable(Table table, String... databases) {
        if (databases == null) {
            throw new RuntimeException("Error while registering table " + table.getName() + ", there is no databases provided in a multidatabase setting.");
        }

        for (String database : databases) {
            MysqlDatabase mysqlDatabase = this.databases.get(database.toLowerCase());
            if (mysqlDatabase == null) {
                logger.severe("Could not find a database with the name " + database);
                continue;
            }
            
            mysqlDatabase.addTable(table);
        }
    }
    
    private Pair<MysqlDatabase, MysqlDataSource> getDatabaseAndSource(IDataObject object) {
        MysqlDatabase mysqlDatabase = this.databases.get(object.getDataInfo().getName());
        MysqlDataSource dataSource = this.dataSources.get(mysqlDatabase.getHost().toLowerCase());
        if (mysqlDatabase == null) {
            logger.severe("The database instance for the record " + object.getClass().getName() + " is null.");
            return null;
        }

        if (dataSource == null) {
            logger.severe("The data source instance for the record " + object.getClass().getName() + " and the database " + mysqlDatabase.getDatabaseName() + " is null");
            return null;
        }
        
        return new Pair<>(mysqlDatabase, dataSource);
    }
    
    public void deleteData(IDataObject object) {
        Pair<MysqlDatabase, MysqlDataSource> result = getDatabaseAndSource(object);
        result.getValue1().deleteData(result.getValue2(), object);
    }

    public void generate() {
        for (MysqlDatabase database : this.databases.values()) {
            MysqlDataSource source = this.dataSources.get(database.getHost().toLowerCase());
            if (source != null) {
                database.generateTables(source);
            }
        }
    }
    
    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        logger.warning("There is a use of a not recommended method in a multidatabase context.");
        List<T> data = new ArrayList<>();
        Table table = this.dataObjectRegistry.getTableByDataClass(recordType);
        
        for (MysqlDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                data.add(database.getData(this.dataSources.get(database.getHost().toLowerCase()), recordType, columnName, value));
            }
        }
        
        if (!data.isEmpty()) {
            return data.get(0);
        }
        
        return null;
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        logger.warning("There is a use of a not recommended method in a multidatabase context.");
        List<T> data = new ArrayList<>();
        Table table = this.dataObjectRegistry.getTableByDataClass(recordType);

        for (MysqlDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                data.addAll(database.getAllData(this.dataSources.get(database.getHost().toLowerCase()), recordType, columnName, value));
            }
        }

        return data;
    }
    
    private <T extends IDataObject> Pair<MysqlDatabase, MysqlDataSource> getSourceAndDatabase(Class<T> recordType, String databaseName) {
        Table table = this.dataObjectRegistry.getTableByDataClass(recordType);
        MysqlDatabase mysqlDatabase = null;

        for (MysqlDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                mysqlDatabase = database;
            }
        }

        if (mysqlDatabase == null) {
            logger.severe("Error while trying to get data from the database " + databaseName + " using the IDataObject " + recordType.getName());
            return null;
        }

        MysqlDataSource source = this.dataSources.get(mysqlDatabase.getHost().toLowerCase());
        return new Pair(mysqlDatabase, source);
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String databaseName, String columnName, Object value) {
        Pair<MysqlDatabase, MysqlDataSource> result = getSourceAndDatabase(recordType, databaseName);
        return result.getValue1().getData(result.getValue2(), recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String databaseName, String columnName, Object value) {
        Pair<MysqlDatabase, MysqlDataSource> result = getSourceAndDatabase(recordType, databaseName);
        return result.getValue1().getAllData(result.getValue2(), recordType, columnName, value);
    }
}
