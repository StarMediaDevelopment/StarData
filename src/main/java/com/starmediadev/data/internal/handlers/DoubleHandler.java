package com.starmediadev.data.internal.handlers;

import com.starmediadev.data.model.DataTypeHandler;
import com.starmediadev.data.model.DataType;

public class DoubleHandler extends DataTypeHandler<Double> {
    public DoubleHandler() {
        super(Double.class, DataType.DOUBLE);
    }

    public boolean matchesType(Class<?> clazz) {
        return super.matchesType(clazz) || clazz.isAssignableFrom(double.class);
    }

    public Object serializeSql(Object object) {
        if (object.getClass().isAssignableFrom(javaClass)) {
            return object;
        }
        return null;
    }

    public Double deserialize(Object object) {
        Double value = null;
        if (object instanceof Double) {
            value = (Double) object;
        } else if (object instanceof String) {
            value = Double.parseDouble((String) object);
        }
        return value;
    }
}
