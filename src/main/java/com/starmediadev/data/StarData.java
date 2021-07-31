package com.starmediadev.data;

import com.starmediadev.data.manager.DatabaseManager;
import com.starmediadev.data.manager.SingleDatabaseManager;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StarData {
    public static String driverClass;

    private TypeRegistry typeRegistry;
    private DataObjectRegistry dataObjectRegistry;

    private DatabaseManager databaseManager;

    private Logger logger;

    static {
        try {
            Class<?> sqlDriver = Class.forName("com.mysql.cj.jdbc.Driver");
            Constructor<?> constructor = sqlDriver.getDeclaredConstructor();
            Object o = constructor.newInstance();
            driverClass = o.getClass().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public StarData(Logger logger) {
        this.logger = logger;

        typeRegistry = TypeRegistry.createInstance(logger);
        dataObjectRegistry = DataObjectRegistry.createInstance(logger, typeRegistry);
        databaseManager = new SingleDatabaseManager(this);
    }

    public static Logger createLogger(Class<?> clazz) {
        InputStream stream = StarData.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            return null;
        }

        Logger logger = Logger.getLogger(clazz.getName());
        try {
            stream.close();
        } catch (IOException e) {
        }
        return logger;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public DataObjectRegistry getDataObjectRegistry() {
        return dataObjectRegistry;
    }

    public Logger getLogger() {
        return logger;
    }
    
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}
