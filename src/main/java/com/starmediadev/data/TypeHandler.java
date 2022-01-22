package com.starmediadev.data;

import java.util.ArrayList;
import java.util.List;

public abstract class TypeHandler<T> {
    protected final Class<T> type;
    protected List<Class<T>> subTypes = new ArrayList<>();
    protected final SQLDataType SQLDataType;
    
    public TypeHandler(Class<T> type, SQLDataType SQLDataType) {
        this.type = type;
        this.SQLDataType = SQLDataType;
    }
    
    public abstract Object serializeSql(T object);
    public abstract String serializeRedis(T object);
    public abstract String serializeJson(T object);
    
    public abstract T deserializeSql(Object object);
    public abstract T deserializeRedis(String str);
    public abstract T deserializeJson(String str);
    
    public Class<T> getType() {
        return type;
    }
    
    public SQLDataType getSQLDataType() {
        return SQLDataType;
    }
}
