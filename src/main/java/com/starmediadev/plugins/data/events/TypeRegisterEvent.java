package com.starmediadev.plugins.data.events;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.utils.collection.ListMap;

import java.util.HashSet;
import java.util.Set;

public class TypeRegisterEvent extends StarDataEvent {
    
    private Set<Class<? extends IDataObject>> dataTypes = new HashSet<>();
    private ListMap<String, String> typeToDatabases = new ListMap();
    
    public TypeRegisterEvent(StarData starData) {
        super(starData);
    }
    
    public void addDataType(Class<? extends IDataObject> object, String... databases) {
        this.dataTypes.add(object);
        if (databases != null) {
            for (String database : databases) {
                typeToDatabases.add(object.getName(), database);
            }
        }
    }

    public Set<Class<? extends IDataObject>> getDataTypes() {
        return dataTypes;
    }

    public ListMap<String, String> getTypeToDatabases() {
        return typeToDatabases;
    }
}
