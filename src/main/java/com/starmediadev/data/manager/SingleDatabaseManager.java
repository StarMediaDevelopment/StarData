package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.SQLDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.model.source.DataSource;
import com.starmediadev.data.model.source.MysqlDataSource;
import com.starmediadev.data.model.source.SqliteDataSource;
import com.starmediadev.data.properties.MysqlProperties;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.properties.SqliteProperties;

import java.util.List;

public class SingleDatabaseManager extends DatabaseManager {

    private SQLDatabase database;

    public SingleDatabaseManager(StarData starData) {
        super(starData);
    }

    public SQLDatabase setupDatabase(SqlProperties properties) {
        DataSource dataSource = null;
        if (properties instanceof MysqlProperties mysqlProperties) {
            dataSource = new MysqlDataSource(mysqlProperties.toJDBCUrl(), mysqlProperties.getUsername(), mysqlProperties.getPassword());
        } else if (properties instanceof SqliteProperties sqliteProperties) {
            dataSource = new SqliteDataSource(sqliteProperties.getFile());
        }
        
        return database = new SQLDatabase(starData, properties, dataSource);
    }

    public void saveData(IDataObject record) {
        database.saveData(record);
    }

    public void saveAllData(IDataObject... records) {
        database.saveAllData(records);
    }

    public void registerTable(Table table, String... databases) {
        database.addTable(table);
        if (this.setup) {
            generate();
        }
    }

    public void deleteData(IDataObject object) {
        this.database.deleteData(object);
    }

    public void generate() {
        database.generateTables();
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        return database.getData(recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        return database.getAllData(recordType, columnName, value);
    }
}