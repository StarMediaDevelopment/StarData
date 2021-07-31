package com.starmediadev.data.registries;

import com.starmediadev.data.annotations.ColumnIgnored;
import com.starmediadev.data.annotations.ColumnInfo;
import com.starmediadev.data.annotations.TableInfo;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.model.Column;
import com.starmediadev.data.model.DataInfo;
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
    private final Set<Class<? extends IDataObject>> types = new HashSet<>();
    private final Set<Table> tables = new HashSet<>();
    private final Logger logger;

    private final Map<String, String> objectTypeToTableMap = new HashMap<>();

    public static DataObjectRegistry createInstance(Logger logger, TypeRegistry typeRegistry) {
        return new DataObjectRegistry(logger, typeRegistry);
    }

    private DataObjectRegistry(Logger logger, TypeRegistry typeRegistry) {
        this.logger = logger;
        this.typeRegistry = typeRegistry;
    }

    public Table register(Class<? extends IDataObject> recordClass) {
        Table table = getTableByDataClass(recordClass);
        if (table == null) {
            try {
                recordClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                logger.severe("Could not find a default contructor for record " + recordClass.getName());
                return null;
            }

            Field dataInfoField = null;
            for (Field classField : Utils.getClassFields(recordClass)) {
                if (DataInfo.class.isAssignableFrom(classField.getType())) {
                    dataInfoField = classField;
                }
            }
            
            if (dataInfoField == null) {
                logger.severe("Could not find a DataInfo field in the IDataObject class " + recordClass.getName());
                return null;
            }

            Set<Field> fields = Utils.getClassFields(recordClass);
            Map<String, Column> columns = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase(dataInfoField.getName())) {
                    columns.put("id", new Column("id", typeRegistry.getHandler(Integer.class), true, true));
                    continue;
                }
                
                if (field.getName().equalsIgnoreCase("id")) {
                    logger.severe("The IDataObject class " + recordClass.getName() + " has a field with the name id that is not the DataInfo field, this cannot be used.");
                    return null;
                }
                ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
                if (columnInfo != null) {
                    if (columnInfo.ignored()) {
                        continue;
                    }
                }

                ColumnIgnored columnIgnored = field.getAnnotation(ColumnIgnored.class);
                if (columnIgnored != null) {
                    continue;
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

                if (columnInfo != null) {
                    colLength = columnInfo.length();
                    if (columnInfo.name() != null && !columnInfo.name().equals("")) {
                        colName = columnInfo.name();
                    }
                }

                handler = typeRegistry.getHandler(field.getType());
                if (handler == null) {
                    logger.severe("Field " + field.getName() + " which has the type " + field.getType().getName() + " of the record " + recordClass.getName() + " is not a record, nor can it be handled.");
                    return null;
                }

                columns.put(field.getName(), new Column(colName, handler, colLength));
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
        types.add(recordClass);
        tables.add(table);
        objectTypeToTableMap.put(recordClass.getName(), table.getName());
        return table;
    }

    public Table getTableByDataClass(Class<? extends IDataObject> recordClass) {
        String tableName = null;
        for (Map.Entry<String, String> entry : objectTypeToTableMap.entrySet()) {
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

    public Class<? extends IDataObject> getDataClassByTable(Table table) {
        for (Class<? extends IDataObject> record : types) {
            if (record.getName().equalsIgnoreCase(table.getRecordName())) {
                return record;
            }
        }
        return null;
    }

    public Class<? extends IDataObject> getClassByName(String name) {
        for (Class<? extends IDataObject> record : types) {
            if (record.getName().equalsIgnoreCase(name)) {
                return record;
            }
        }
        return null;
    }
}
