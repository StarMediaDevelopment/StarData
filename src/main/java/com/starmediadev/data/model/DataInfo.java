package com.starmediadev.data.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DataInfo {
    private int id; 
    private String name;

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
    
    public String toString() {
        return "DataInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", databaseIdMap=" + databaseIdMap +
                ", databases=" + databases +
                '}';
    }
}
