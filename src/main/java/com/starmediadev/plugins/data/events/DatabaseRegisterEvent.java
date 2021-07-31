package com.starmediadev.plugins.data.events;

import com.starmediadev.data.StarData;
import com.starmediadev.data.properties.SqlProperties;

import java.util.HashSet;
import java.util.Set;

public class DatabaseRegisterEvent extends StarDataEvent {
    
    private Set<SqlProperties> databaseProperties = new HashSet<>();
    
    public DatabaseRegisterEvent(StarData starData) {
        super(starData);
    }
    
    public void addDatabaseDetails(SqlProperties properties) {
        this.databaseProperties.add(properties);
    }

    public Set<SqlProperties> getDatabaseProperties() {
        return databaseProperties;
    }
}
