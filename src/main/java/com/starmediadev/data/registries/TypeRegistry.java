package com.starmediadev.data.registries;

import com.starmediadev.data.internal.handlers.*;
import com.starmediadev.data.model.DataTypeHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class TypeRegistry {
    private final Logger logger;
    private final Set<DataTypeHandler<?>> typeHandlers = new HashSet<>();

    private TypeRegistry(Logger logger) {
        this.logger = logger;
        typeHandlers.add(new ArrayHandler(this));
        typeHandlers.add(new BooleanHandler());
        typeHandlers.add(new CollectionHandler());
        typeHandlers.add(new DoubleHandler());
        typeHandlers.add(new EnumHandler());
        typeHandlers.add(new FloatHandler());
        typeHandlers.add(new IntegerHandler());
        typeHandlers.add(new LongHandler());
        typeHandlers.add(new DataObjectHandler());
        typeHandlers.add(new StringHandler());
        typeHandlers.add(new UUIDHandler());
    }
    
    public static TypeRegistry createInstance(Logger logger) {
        return new TypeRegistry(logger);
    }
    
    
    public void register(DataTypeHandler<?> handler) {
        for (DataTypeHandler<?> typeHandler : typeHandlers) {
            if (typeHandler.getJavaClass().isAssignableFrom(handler.getJavaClass())) {
                throw new IllegalArgumentException("A handler for the Java Class " + handler.getJavaClass().getName() + " is already registered!");
            }

            for (Class<?> addClass : handler.getAdditionalClasses()) {
                if (typeHandler.getJavaClass().isAssignableFrom(addClass)) {
                    throw new IllegalArgumentException("A handler for the Java Class " + handler.getJavaClass().getName() + " is already registered!");
                }
            }

            for (Class<?> addClass : typeHandler.getAdditionalClasses()) {
                if (handler.getJavaClass().isAssignableFrom(addClass)) {
                    throw new IllegalArgumentException("A handler for the Java Class " + handler.getJavaClass().getName() + " is already registered!");
                }
            }
        }
        
        typeHandlers.add(handler);
    }

    public Set<DataTypeHandler<?>> getTypeHandlers() {
        return typeHandlers;
    }

    public DataTypeHandler<?> getHandler(Class<?> typeClass) {
        for (DataTypeHandler<?> typeHandler : typeHandlers) {
            if (typeHandler.matchesType(typeClass)) {
                return typeHandler;
            }
        }
        return null;
    }
}
