package com.starmediadev.sql.handlers;

import com.starmediadev.sql.model.DataType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CollectionHandler extends DataTypeHandler<Collection> {
    public CollectionHandler() {
        super(Collection.class, DataType.VARCHAR, 1000);
        addAdditionClass(List.class);
        addAdditionClass(Set.class);
    }

    public Object serializeSql(Object type) {
        return type;
    }

    public Collection deserialize(Object object) {
        
        return null;
    }
}