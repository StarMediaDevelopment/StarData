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
    private Context context;
    
    private TypeRegistry typeRegistry;
    private DataObjectRegistry dataObjectRegistry;
    
    private DatabaseManager manager;
    
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
    
    public StarData(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
        
        typeRegistry = TypeRegistry.createInstance(logger);
        dataObjectRegistry = DataObjectRegistry.createInstance(logger, typeRegistry);
        
        if (context == Context.SINGLE) {
            manager = new SingleDatabaseManager(logger, dataObjectRegistry, typeRegistry);
        } else if (context == Context.MULTI) {
            throw new IllegalStateException("Multidatabase support is not yet implemented.");
            // manager = new MultiDatabaseManager(logger, dataObjectRegistry, typeRegistry);
        }
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
        } catch (IOException e) {}
        return logger;
    }
    
    public DatabaseManager getDatabaseManager() {
        return manager;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public DataObjectRegistry getRecordRegistry() {
        return dataObjectRegistry;
    }

    public Logger getLogger() {
        return logger;
    }
}
