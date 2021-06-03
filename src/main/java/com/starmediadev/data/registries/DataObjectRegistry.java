package com.starmediadev.data.registries;

import com.starmediadev.data.annotations.ColumnInfo;
import com.starmediadev.data.annotations.TableInfo;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.model.Column;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.model.Table;
import com.starmediadev.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

public final class DataObjectRegistry {
    private final TypeRegistry typeRegistry;
    private final Set<Class<? extends IDataObject>> records = new HashSet<>();
    private final Set<Table> tables = new HashSet<>();
    private final Logger logger;
    
    private final Map<String, String> recordToTableMap = new HashMap<>();
    
    public static DataObjectRegistry createInstance(Logger logger, TypeRegistry typeRegistry) {
        return new DataObjectRegistry(logger, typeRegistry);
    }
    
    private DataObjectRegistry(Logger logger, TypeRegistry typeRegistry) {
        this.logger = logger;
        this.typeRegistry = typeRegistry;
    }
    
    public Table register(Class<? extends IDataObject> recordClass) {
        Table table = getTableByRecordClass(recordClass);
        if (table == null) {
            try {
                recordClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                logger.severe("Could not find a default contructor for record " + recordClass.getName());
                return null;
            }

            try {
                Field id = recordClass.getDeclaredField("id");
                if (!(id.getType().isAssignableFrom(int.class) || id.getType().isAssignableFrom(long.class))) {
                    logger.severe("The ID field is not of type int or long");
                    return null;
                }
            } catch (NoSuchFieldException e) {
                logger.severe("Could not find an id field in the record " + recordClass.getName());
                return null;
            }

            Set<Field> fields = Utils.getClassFields(recordClass);
            Map<String, Column> columns = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
                if (columnInfo != null) {
                    if (columnInfo.ignored()) {
                        continue;
                    }
                }
                
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType pt) {
                        Type[] types = pt.getActualTypeArguments();
                        if (types != null && types.length == 1) {
                            if (types[0] instanceof Class<?> ptc) {
                                if (!IDataObject.class.isAssignableFrom(ptc) && typeRegistry.getHandler(ptc) == null) {
                                    logger.severe("Collection type " + types[0].getTypeName() + " for the field " + field.getName() + " of the record " + recordClass.getName() + " cannot be handled.");
                                    return null;
                                }
                            }
                        }
                    }
                }
                
                if (field.getType().isArray()) {
                    Class<?> compType = field.getType().getComponentType();
                    if (!IDataObject.class.isAssignableFrom(compType) && typeRegistry.getHandler(compType) == null) {
                        logger.severe("Array type " + compType.getTypeName() + " for the field " + field.getName() + " of the record " + recordClass.getName() + " cannot be handled.");
                        return null;
                    }
                }

                DataTypeHandler<?> handler;
                String colName = field.getName();
                int colLength = 0;
                boolean colAutoIncrement = false, colUnique = false;

                if (columnInfo != null) {
                    colLength = columnInfo.length();
                    //colAutoIncrement = columnInfo.autoIncrement();
                    //colUnique = columnInfo.unique();
                    if (columnInfo.name() != null && !columnInfo.name().equals("")) {
                        colName = columnInfo.name();
                    }
                }

                handler = typeRegistry.getHandler(field.getType());
                if (handler == null) {
                    logger.severe("Field " + field.getName() + " which has the type " + field.getType().getName() + " of the record " + recordClass.getName() + " is not a record, nor can it be handled.");
                    return null;
                }
                
                if (field.getName().equalsIgnoreCase("id")) {
                    if (field.getType().isAssignableFrom(int.class) || field.getType().isAssignableFrom(long.class)) {
                        colAutoIncrement = true;
                        colUnique = true;
                    }
                }

                columns.put(field.getName(), new Column(colName, handler, colLength, colAutoIncrement, colUnique));
            }

            String tableName = "";
            TableInfo tableInfo = recordClass.getAnnotation(TableInfo.class);
            if (tableInfo != null) {
                if (!tableInfo.tableName().equals("")) {
                    tableName = tableInfo.tableName();
                }
            } else {
                tableName = recordClass.getSimpleName().toLowerCase();
            }
            table = new Table(tableName, columns.values());
        }

        table.setRecordName(recordClass.getName());
        records.add(recordClass);
        tables.add(table);
        recordToTableMap.put(recordClass.getName(), table.getName());
        return table;
    }

    public Table getTableByRecordClass(Class<? extends IDataObject> recordClass) {
        String tableName = null;
        for (Map.Entry<String, String> entry : recordToTableMap.entrySet()) {
            if (recordClass.getName().equalsIgnoreCase(entry.getKey())) {
                tableName = entry.getValue();
            }
        }
        
        if (tableName == null) {
            return null;
        }

        for (Table table : tables) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }

        return null;
    }

    public Class<? extends IDataObject> getRecordClassByTable(Table table) {
        for (Class<? extends IDataObject> record : records) {
            if (record.getName().equalsIgnoreCase(table.getRecordName())) {
                return record;
            }
        }
        return null;
    }

    public Class<? extends IDataObject> getRecordByClassName(String name) {
        for (Class<? extends IDataObject> record : records) {
            if (record.getName().equalsIgnoreCase(name)) {
                return record;
            }
        }
        return null;
    }
}