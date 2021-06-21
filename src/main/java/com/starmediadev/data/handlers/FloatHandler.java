package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;

public class FloatHandler extends DataTypeHandler<Float> {
    public FloatHandler() {
        super(Float.class, DataType.DOUBLE);
    }

    public boolean matchesType(Class<?> clazz) {
        return super.matchesType(clazz) || clazz.isAssignableFrom(float.class);
    }

    public Object serializeSql(Object object) {
        if (object.getClass().isAssignableFrom(javaClass)) {
            return object;
        }
        return null;
    }

    public Float deserialize(Object object) {
        Float value = null;
        if (object instanceof Float) {
            value = (Float) object;
        } else if (object instanceof String) {
            value = Float.parseFloat((String) object);
        }
        return value;
    }
}
