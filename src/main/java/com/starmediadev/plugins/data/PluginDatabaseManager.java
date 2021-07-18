package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import com.starmediadev.data.manager.DatabaseManager;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.model.Table;
import com.starmediadev.data.properties.SqlProperties;

import java.util.List;

public class PluginDatabaseManager extends DatabaseManager {
    public PluginDatabaseManager(StarData starData) {
        super(starData.getLogger(), starData.getRecordRegistry(), starData.getTypeRegistry());
    }

    public PluginDatabase setupDatabase(SqlProperties properties) {
        return null;
    }

    public void saveData(IDataObject record) { 
        
    }

    public void saveAllData(IDataObject... records) { 
        
    }

    public void registerTable(Table table) { 
        
    }

    public void deleteData(IDataObject object) { 
        
    }

    public void generate() {

    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        return null;
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        return null;
    }
}
