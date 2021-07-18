package com.starmediadev.data.manager;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.MysqlDataSource;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;

import java.util.List;

public class SingleDatabaseManager extends DatabaseManager {

    private MysqlDatabase database;
    private MysqlDataSource dataSource;

    public SingleDatabaseManager(StarData starData) {
        super(starData);
    }

    public MysqlDatabase setupDatabase(SqlProperties properties) {
        this.dataSource = new MysqlDataSource(URL.replace("{host}", properties.getHost()).replace("port", properties.getPort() + ""), properties.getUsername(), properties.getPassword() + "/" + properties.getDatabase());
        return database = new MysqlDatabase(starData, properties);
    }

    public void saveData(IDataObject record) {
        database.saveData(dataSource, record);
    }

    public void saveAllData(IDataObject... records) {
        database.saveAllData(dataSource, records);
    }

    public void registerTable(Table table, String... databases) {
        database.addTable(table);
        if (this.setup) {
            generate();
        }
    }

    public void deleteData(IDataObject object) {
        this.database.deleteData(dataSource, object);
    }

    public void generate() {
        database.generateTables(dataSource);
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        return database.getData(dataSource, recordType, columnName, value);
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        return database.getAllData(dataSource, recordType, columnName, value);
    }
}