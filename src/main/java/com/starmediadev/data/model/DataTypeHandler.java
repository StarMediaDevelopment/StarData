package com.starmediadev.data.model;

import com.starmediadev.data.model.DataType;

import java.util.HashSet;
import java.util.Set;

public abstract class DataTypeHandler<T> {
    
    protected Class<?> javaClass;
    protected final Set<Class<?>> additionalClasses = new HashSet<>();
    protected DataType mysqlType;
    protected int defaultLength;

    public DataTypeHandler(Class<?> javaClass, DataType mysqlType) {
        this.javaClass = javaClass;
        this.mysqlType = mysqlType;
    }
    
    public void addAdditionClass(Class<?> clazz) {
        this.additionalClasses.add(clazz);
    }

    public DataTypeHandler(Class<?> javaClass, DataType mysqlType, int defaultLength) {
        this(javaClass, mysqlType);
        this.defaultLength = defaultLength;
    }

    protected DataTypeHandler() {
    }

    public boolean matchesType(Class<?> clazz) {
        boolean matches = clazz.isAssignableFrom(javaClass);
        if (!matches) {
            matches = javaClass.isAssignableFrom(clazz);
        }
        if (!matches) {
            if (!this.additionalClasses.isEmpty()) {
                for (Class<?> additionalClass : this.additionalClasses) {
                    if (additionalClass.isAssignableFrom(clazz)) {
                        matches = true;
                    }
                }
            }
        }
        return matches;
    }

    public abstract Object serializeSql(Object object);

    public T deserialize(Object object, Class<?> typeClass) {
        return deserialize(object);
    }

    public T deserialize(Object object) {
        return null;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public Set<Class<?>> getAdditionalClasses() {
        return additionalClasses;
    }

    public DataType getMysqlType() {
        return mysqlType;
    }

    public int getDefaultLength() {
        return defaultLength;
    }
}
