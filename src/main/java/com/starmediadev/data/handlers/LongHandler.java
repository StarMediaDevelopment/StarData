package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;

public class LongHandler extends DataTypeHandler<Long> {
    public LongHandler() {
        super(Long.class, DataType.BIGINT);
    }

    public boolean matchesType(Class<?> clazz) {
        return super.matchesType(clazz) || clazz.isAssignableFrom(long.class);
    }

    public Object serializeSql(Object object) {
        return object;
    }

    public Long deserialize(Object object) {
        Long value = null;
        if (object instanceof Long) {
            value = (Long) object;
        } else if (object instanceof String) {
            value = Long.parseLong((String) object);
        }
        return value;
    }
}
