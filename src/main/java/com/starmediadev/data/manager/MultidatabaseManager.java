package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.SQLDatabase;
import com.starmediadev.data.model.source.DataSource;
import com.starmediadev.data.model.source.MysqlDataSource;
import com.starmediadev.data.internal.objects.Table;
import com.starmediadev.data.model.source.SqliteDataSource;
import com.starmediadev.data.properties.MysqlProperties;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.properties.SqliteProperties;
import com.starmediadev.utils.helper.StringHelper;

import java.util.*;

public class MultidatabaseManager extends DatabaseManager {

    private Map<String, SQLDatabase> databases = new HashMap<>();
    private Set<Table> allDatabaseTables = new HashSet<>();

    public MultidatabaseManager(StarData starData) {
        super(starData);
    }

    public SQLDatabase setupDatabase(SqlProperties properties) {
        DataSource dataSource = null;
        String databaseName = "";
        if (properties instanceof MysqlProperties mysqlProperties) {
            dataSource = new MysqlDataSource(mysqlProperties.toJDBCUrl(), mysqlProperties.getDatabase(), mysqlProperties.getUsername(), mysqlProperties.getPassword());
            databaseName = mysqlProperties.getDatabase().toLowerCase();
        } else if (properties instanceof SqliteProperties sqliteProperties) {
            dataSource = new SqliteDataSource(sqliteProperties.getFile());
            String fileName = sqliteProperties.getFile().getFileName().toString();
            databaseName = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();
        }
        
        if (this.databases.containsKey(databaseName)) {
            throw new RuntimeException("Database " + databaseName + " already exists.");
        }

        SQLDatabase mysqlDatabase = new SQLDatabase(starData, properties, dataSource);
        if (!this.allDatabaseTables.isEmpty()) {
            for (Table table : this.allDatabaseTables) {
                mysqlDatabase.addTable(table);
                table.addDatabase(mysqlDatabase.getName());
            }
        }
        this.databases.put(mysqlDatabase.getName(), mysqlDatabase);
        return mysqlDatabase;
    }

    public void saveData(IDataObject record) {
        logger.finest("Saving an object with the type " + record.getClass().getName());
        List<String> rawDatabases = new ArrayList<>();

        if (record.getDataInfo().getMappings().isEmpty()) {
            logger.finest("No current database mappings exist, adding specified databases");
            rawDatabases.addAll(record.getDataInfo().getDatabases());
        } else {
            logger.finest("Database mappings exist for the data object");
            rawDatabases.addAll(record.getDataInfo().getMappings().keySet());
        }

        Table table = dataObjectRegistry.getTableByDataClass(record.getClass());
        logger.finest("Table for the record is " + table.getName());
        if (rawDatabases.isEmpty()) {
            logger.finest("No databases existed from object data, adding databases from the table configuration");
            rawDatabases.addAll(table.getDatabases());
            logger.finest("Configured Table databases: " + StringHelper.join(table.getDatabases(), ","));
        }
        List<SQLDatabase> databases = new ArrayList<>();
        if (!rawDatabases.isEmpty()) {
            logger.finest("Checking all databases specified");
            for (String database : rawDatabases) {
                logger.finest("Checking database " + database);
                SQLDatabase mysqlDatabase = this.databases.get(database);
                if (mysqlDatabase == null) {
                    return;
                }

                databases.add(mysqlDatabase);
                logger.finest("Successfully checked database " + database);
            }
        } else {
            logger.finest("No databases provided, checking against all databases and registered tables");
            for (SQLDatabase database : this.databases.values()) {
                logger.finest("Checking database " + database.getName());
                if (database.getTables().containsKey(table.getName())) {
                    logger.finest("Database " + database.getName() + " has the table " + table.getName() + " registered");
                    databases.add(database);
                }
            }
        }

        logger.finest("Found a total of " + databases.size() + " databases that this data object can be saved to.");

        for (SQLDatabase database : databases) {
            if (database.getTables().containsKey(table.getName())) {
                logger.info("Saving the data to the database " + database.getName());
                database.saveData(record);
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
        logger.finest("Registering the table " + table.getName());
        if (databases != null) {
            logger.finest("Specified databases is not null");
            for (String database : databases) {
                logger.finest("Checking database name " + database);
                SQLDatabase mysqlDatabase = this.databases.get(database.toLowerCase());
                if (mysqlDatabase == null) {
                    logger.severe("Could not find a database with the name " + database);
                    continue;
                }

                mysqlDatabase.addTable(table);
                table.addDatabase(mysqlDatabase.getName());
                logger.finest("Registered table " + table.getName() + " with the database " + database);
            }
        } else {
            logger.finest("No databases provided, this is now an all database table.");
            this.allDatabaseTables.add(table);
            for (SQLDatabase database : this.databases.values()) {
                database.addTable(table);
                table.addDatabase(database.getName());
                logger.finest("Registered table " + table.getName() + " with the database " + database.getName());
            }
        }
    }

    public void deleteData(IDataObject object) {
        object.getDataInfo().getMappings().forEach((database, id) -> {
            SQLDatabase mysqlDatabase = this.databases.get(database);
            if (mysqlDatabase == null) {
                return;
            }

            Table table = dataObjectRegistry.getTableByDataClass(object.getClass());
            if (mysqlDatabase.getTables().containsKey(table.getName())) {
                mysqlDatabase.deleteData(object);
            }
        });
    }

    public void generate() {
        for (SQLDatabase database : this.databases.values()) {
            database.generateTables();
        }
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        List<T> data = new ArrayList<>();
        Table table = this.dataObjectRegistry.getTableByDataClass(recordType);

        for (SQLDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                data.add(database.getData(recordType, columnName, value));
            }
        }

        if (!data.isEmpty()) {
            return data.get(0);
        }

        return null;
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        List<T> data = new ArrayList<>();
        Table table = this.dataObjectRegistry.getTableByDataClass(recordType);

        for (SQLDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                data.addAll(database.getAllData(recordType, columnName, value));
            }
        }

        return data;
    }

    private <T extends IDataObject> SQLDatabase getDatabaseFromDataType(Class<T> dataType, String databaseName) {
        Table table = this.dataObjectRegistry.getTableByDataClass(dataType);
        SQLDatabase mysqlDatabase = null;

        for (SQLDatabase database : this.databases.values()) {
            if (database.getTables().containsKey(table.getName())) {
                mysqlDatabase = database;
            }
        }

        if (mysqlDatabase == null) {
            logger.severe("Error while trying to get data from the database " + databaseName + " using the IDataObject " + dataType.getName());
            return null;
        }
        return mysqlDatabase;
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String databaseName, String columnName, Object value) {
        SQLDatabase result = getDatabaseFromDataType(recordType, databaseName);
        return result.getData(recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String databaseName, String columnName, Object value) {
        SQLDatabase result = getDatabaseFromDataType(recordType, databaseName);
        return result.getAllData(recordType, columnName, value);
    }
}
