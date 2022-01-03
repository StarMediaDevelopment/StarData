package com.starmediadev.data;

import java.util.ArrayList;
import java.util.List;

public abstract class TypeHandler<T> {
    protected Class<T> type;
    protected List<Class<T>> subTypes = new ArrayList<>();
    protected SQLDataType SQLDataType;
    
    public TypeHandler(Class<T> type, SQLDataType SQLDataType) {
        this.type = type;
        this.SQLDataType = SQLDataType;
    }
    
    public abstract Object serializeSql(T object);
    public abstract String serializeRedis(T object);
    public abstract String serializeJson(T object);
    
    public Class<T> getType() {
        return type;
    }
    
    public SQLDataType getSQLDataType() {
        return SQLDataType;
    }
}
