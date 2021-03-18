package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;
import com.starmediadev.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumHandler extends DataTypeHandler<Enum> {
    public EnumHandler() {
        super(Enum.class, DataType.VARCHAR, 100);
    }

    public Object serializeSql(Object type) {
        if (this.javaClass.isAssignableFrom(type.getClass())) {
            try {
                Method nameMethod = Utils.getMethod(type.getClass(), "name");
                return type.getClass().getName() + ":" + nameMethod.invoke(type);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return type;
    }

    public Enum deserialize(Object object) {
        if (object instanceof String) {
            String str = (String) object;
            String[] split = str.split(":");
            String className = split[0];
            try {
                Class<? extends Enum> clazz = (Class<? extends Enum>) Class.forName(className);
                return Enum.valueOf(clazz, split[1]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        return null;
    }
}
