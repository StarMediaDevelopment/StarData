package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;

public class StringHandler extends DataTypeHandler<String> {
    public StringHandler() {
        super(String.class, DataType.VARCHAR, 100);
    }

    public Object serializeSql(Object type) {
        return type;
    }

    public String deserialize(Object object) {
        return String.valueOf(object);
    }
}