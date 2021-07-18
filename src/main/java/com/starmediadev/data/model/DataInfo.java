package com.starmediadev.data.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DataInfo {
    private int id; //Like the deprecated field in IDataObject this is the entry id from the database
    private String name; //This is the name of the database that the object was saved to.

    private Map<String, Integer> databaseIdMap = new HashMap<>();
    private Set<String> databases = new HashSet<>();

    public DataInfo() {

    }
    
    public void addInitalDatabase(String database) {
        this.databases.add(database);
    }

    public Set<String> getDatabases() {
        return databases;
    }

    public Map<String, Integer> getMappings() {
        return databaseIdMap;
    }

    public void addMapping(String database, Integer id) {
        this.databaseIdMap.put(database, id);
    }

    public Integer getId(String database) {
        return this.databaseIdMap.get(database);
    }
    
    public int getLowestId() {
        int lowest = 0;
        for (Integer value : this.databaseIdMap.values()) {
            if (lowest == 0) {
                lowest = value;
            } else {
                if (value < lowest) {
                    lowest = value;
                }
            }
        }
        return lowest;
    }
}
