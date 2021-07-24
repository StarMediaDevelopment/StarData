package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.MysqlDataSource;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.utils.Pair;
import com.starmediadev.utils.Utils;

import java.util.*;

public class MultidatabaseManager extends DatabaseManager {
    
    private Map<String, MysqlDatabase> databases = new HashMap<>();
    private Map<String, MysqlDataSource> dataSources = new HashMap<>();
    private Set<Table> allDatabaseTables = new HashSet<>();
    
    public MultidatabaseManager(StarData starData) {
        super(starData);
    }

    public MysqlDatabase setupDatabase(SqlProperties properties) {
        if (!dataSources.containsKey(properties.getHost().toLowerCase())) {
            this.dataSources.put(properties.getHost().toLowerCase(), new MysqlDataSource(URL.replace("{hostname}", properties.getHost()).replace("{port}", properties.getPort() + ""), properties.getUsername(), properties.getPassword()));
        }
        
        if (this.databases.containsKey(properties.getDatabase().toLowerCase())) {
            throw new RuntimeException("Database " + properties.getDatabase() + " already exists.");
        }
        
        MysqlDatabase mysqlDatabase = new MysqlDatabase(starData, properties);
        if (!this.allDatabaseTables.isEmpty()) {
            for (Table table : this.allDatabaseTables) {
                mysqlDatabase.addTable(table);
                table.addDatabase(mysqlDatabase.getDatabaseName());
            }
        }
        this.databases.put(mysqlDatabase.getDatabaseName(), mysqlDatabase);
        return mysqlDatabase;
    }
    
    public void saveData(IDataObject record) {
        logger.info("Saving an object with the type " + record.getClass().getName());
        List<String> rawDatabases = new ArrayList<>();
        
        if (record.getDataInfo().getMappings().isEmpty()) {
            logger.info("No current database mappings exist, adding specified databases");
            rawDatabases.addAll(record.getDataInfo().getDatabases());
        } else {
            logger.info("Database mappings exist for the data object");
            rawDatabases.addAll(record.getDataInfo().getMappings().keySet());
        }

        Table table = dataObjectRegistry.getTableByDataClass(record.getClass());
        logger.info("Table for the record is " + table.getName());
        if (rawDatabases.isEmpty()) {
            logger.info("No databases existed from object data, adding databases from the table configuration");
            rawDatabases.addAll(table.getDatabases());
            logger.info("Configured Table databases: " + Utils.join(table.getDatabases(), ","));
        }
        List<MysqlDatabase> databases = new ArrayList<>();
        if (!rawDatabases.isEmpty()) {
            logger.info("Checking all databases specified");
            for (String database : rawDatabases) {
                logger.info("Checking database " + database);
                MysqlDatabase mysqlDatabase = this.databases.get(database);
                if (mysqlDatabase == null) {
                    return;
                }
                
                databases.add(mysqlDatabase);
                logger.info("Successfully checked database " + database);
            }
        } else {
            logger.info("No databases provided, checking against all databases and registered tables");
            for (MysqlDatabase database : this.databases.values()) {
                logger.info("Checking database " + database.getDatabaseName());
                if (database.getTables().containsKey(table.getName())) {
                    logger.info("Database " + database.getDatabaseName() + " has the table " + table.getName() + " registered");
                   databases.add(database);
                }
            }
        }
        
        logger.info("Found a total of " + databases.size() + " databases that this data object can be saved to.");

        for (MysqlDatabase database : databases) {
            if (database.getTables().containsKey(table.getName())) {
                logger.info("Saving the data to the database " + database.getDatabaseName());
                MysqlDataSource dataSource = this.dataSources.get(database.getHost().toLowerCase());
                database.saveData(dataSource, record);
                logger.info("Saved successful");
            }
        }
    }

    public void saveAllData(IDataObject... records) {
        if (records != null) {
            for (IDataObject record : records) {
                saveData(record);
            }
        }
    }

    public void registerTable(Table table, String... databases) {
        logger.info("Registering the table " + table.getName());
        if (databases != null) {
            logger.info("Specified databases is not null");
            for (String database : databases) {
                logger.info("Checking database name " + database);
                MysqlDatabase mysqlDatabase = this.databases.get(database.toLowerCase());
                if (mysqlDatabase == null) {
                    logger.severe("Could not find a database with the name " + database);
                    continue;
                }
                
                mysqlDatabase.addTable(table);
                table.addDatabase(mysqlDatabase.getDatabaseName());
                logger.info("Registered table " + table.getName() + " with the database " + database);
            }
        } else {
            logger.info("No databases provided, this is now an all database table.");
            this.allDatabaseTables.add(table);
            for (MysqlDatabase database : this.databases.values()) {
                database.addTable(table);
                table.addDatabase(database.getDatabaseName());
                logger.info("Registered table " + table.getName() + " with the database " + database.getDatabaseName());
            }
        }
    }
    
    public void deleteData(IDataObject object) {
        object.getDataInfo().getMappings().forEach((database, id) -> {
            MysqlDatabase mysqlDatabase = this.databases.get(database);
            if (mysqlDatabase == null) {
                return;
            }

            Table table = dataObjectRegistry.getTableByDataClass(object.getClass());
            if (mysqlDatabase.getTables().containsKey(table.getName())) {
                MysqlDataSource dataSource = this.dataSources.get(mysqlDatabase.getHost().toLowerCase());
                mysqlDatabase.deleteData(dataSource, object);
            }
        });
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
