package com.starmediadev.data;

import com.starmediadev.utils.helper.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class is for shared methods and functionality between all features and abilities in this library to prevent repeat code
 */
public final class DataUtils {
    private DataUtils() {}
    
    private static final Map<String, Set<Field>> classFields = new HashMap<>();
    
    /**
     * Gets all fields from a class recursively and also filters out transient and static fields and caches the list in memory to help with performance
     * @param clazz The class to check
     * @return The Set of fields
     */
    public static Set<Field> getClassFields(Class<?> clazz) {
        if (classFields.containsKey(clazz.getName())) {
            return classFields.get(clazz.getName());
        }
        Set<Field> classFields = ReflectionHelper.getClassFields(clazz);
        classFields.removeIf(field -> Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()));
        Set<Field> fields = Collections.unmodifiableSet(classFields);
        DataUtils.classFields.put(clazz.getName(), fields);
        return fields;
    }
}
