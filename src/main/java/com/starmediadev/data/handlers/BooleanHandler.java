package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;

public class BooleanHandler extends DataTypeHandler<Boolean> {
    public BooleanHandler() {
        super(Boolean.class, DataType.VARCHAR, 5);
        additionalClasses.add(boolean.class);
    }

    public Object serializeSql(Object object) {
        if (object.getClass().isAssignableFrom(javaClass)) {
            return object + "";
        }
        return null;
    }

    public Boolean deserialize(Object object) {
        Boolean value = null;
        if (object instanceof Boolean b) {
            value = b;
        } else if (object instanceof String str) {
            value = Boolean.parseBoolean(str);
        }
        return value;
    }

    public String serializeRedis(Object object) {
        return Boolean.toString((boolean) object);
    }
}
