package com.starmediadev.sql.handlers;

import com.starmediadev.sql.model.DataType;

import java.util.UUID;

public class UUIDHandler extends DataTypeHandler<UUID> {

    public UUIDHandler() {
        super(UUID.class, DataType.VARCHAR, 36);
    }

    public Object serializeSql(Object object) {
        if (object.getClass().isAssignableFrom(javaClass)) {
            return ((UUID) object).toString();
        }
        return null;
    }

    public UUID deserialize(Object object) {
        if (object instanceof String) {
            return UUID.fromString((String) object);
        }
        return null;
    }

    public String serializeRedis(Object object) {
        if (object instanceof UUID) {
            return ((UUID) object).toString();
        }
        
        return null;
    }
}
