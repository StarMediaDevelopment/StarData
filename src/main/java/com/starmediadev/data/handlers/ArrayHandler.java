package com.starmediadev.data.handlers;

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
        if (rawClass.equals("int"))
            typeClass = int.class;
        else if (rawClass.equals("boolean"))
            typeClass = boolean.class;
        else if (rawClass.equals("long"))
            typeClass = long.class;
        else if (rawClass.equals("double"))
            typeClass = double.class;
        else {
            try {
                typeClass = Class.forName(rawClass);
            } catch (Exception e) {
                return null;
            }
        }

        String[] elementValues = valueSplit[1].split(",");

        Object array = Array.newInstance(typeClass, elementValues.length);

        DataTypeHandler<?> typeHandler = typeRegistry.getHandler(typeClass);
        for (int i = 0; i < elementValues.length; i++) {
            Array.set(array, i, typeHandler.deserialize(elementValues[i], typeClass));
        }

        return array;
    }
}