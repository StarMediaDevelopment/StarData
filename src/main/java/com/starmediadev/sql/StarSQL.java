package com.starmediadev.sql;

import com.starmediadev.sql.manager.DatabaseManager;
import com.starmediadev.sql.manager.MultiDatabaseManager;
import com.starmediadev.sql.manager.SingleDatabaseManager;
import com.starmediadev.sql.registries.RecordRegistry;
import com.starmediadev.sql.registries.TypeRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StarSQL {
    public static String DRIVER_CLASS;
    private Context context;
    
    private TypeRegistry typeRegistry;
    private RecordRegistry recordRegistry;
    
    private DatabaseManager manager;
    
    private Logger logger;
    
    static {
        try {
            Object o = Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            DRIVER_CLASS = o.getClass().getName();
            System.out.println(DRIVER_CLASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public StarSQL(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
        
        typeRegistry = TypeRegistry.createInstance(logger);
        recordRegistry = RecordRegistry.createInstance(logger, typeRegistry);
        
        if (context == Context.SINGLE) {
            manager = new SingleDatabaseManager(logger, recordRegistry, typeRegistry);
        } else if (context == Context.MULTI) {
            manager = new MultiDatabaseManager(logger, recordRegistry, typeRegistry);
        }
    }
    
    public static Logger createLogger(Class<?> clazz) {
        InputStream stream = StarSQL.class.getClassLoader().getResourceAsStream("logging.properties");
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

    public RecordRegistry getRecordRegistry() {
        return recordRegistry;
    }

    public Logger getLogger() {
        return logger;
    }
}
