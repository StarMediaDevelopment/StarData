package com.starmediadev.data.internal.handlers;

import com.starmediadev.data.model.DataTypeHandler;
import com.starmediadev.data.model.DataType;
import com.starmediadev.data.registries.TypeRegistry;

import java.lang.reflect.Array;

public class ArrayHandler extends DataTypeHandler<Object> {

    private final TypeRegistry typeRegistry;

    public ArrayHandler(TypeRegistry typeRegistry) {
        super(Object[].class, DataType.VARCHAR, 1000);
        this.typeRegistry = typeRegistry;
    }

    public boolean matchesType(Class<?> clazz) {
        return clazz.isArray();
    }

    public Object serializeSql(Object object) {
        if (!object.getClass().isArray())
            return null;
        int length = Array.getLength(object);
        StringBuilder sb = new StringBuilder();
        sb.append(object.getClass().getComponentType().getName()).append(":");
        for (int i = 0; i < length; i++) {
            Object element = Array.get(object, i);
            sb.append(element.toString());

            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public Object deserialize(Object object) {
        String value = (String) object;
        String[] valueSplit = value.split(":");
        Class<?> typeClass;
        String rawClass = valueSplit[0];
        typeClass = switch (rawClass) {
            case "int" -> int.class;
            case "boolean" -> boolean.class;
            case "long" -> long.class;
            case "double" -> double.class;
            default -> {
                try {
                    yield Class.forName(rawClass);
                } catch (Exception e) {
                    yield null;
                }
            }
        };
        
        Object array;
        if (valueSplit.length == 2) {
            String[] elementValues = valueSplit[1].split(",");

            array = Array.newInstance(typeClass, elementValues.length);

            DataTypeHandler<?> typeHandler = typeRegistry.getHandler(typeClass);
            for (int i = 0; i < elementValues.length; i++) {
                Array.set(array, i, typeHandler.deserialize(elementValues[i], typeClass));
            }
        } else {
            array = Array.newInstance(typeClass, 0);
        }

        return array;
    }
}