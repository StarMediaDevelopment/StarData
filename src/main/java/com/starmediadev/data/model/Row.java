package com.starmediadev.data.model;

import com.starmediadev.data.StarData;
import com.starmediadev.data.annotations.ColumnInfo;
import com.starmediadev.data.handlers.CollectionHandler;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.handlers.DataObjectHandler;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Logger;

class Row {
    private final StarData starData;
    private final MysqlDatabase database;
    protected final Map<String, Object> dataMap = new HashMap<>();
    protected final Table table;

    public Row(Table table, ResultSet resultSet, MysqlDatabase database, StarData starData) {
        this.starData = starData;
        this.table = table;
        this.database = database;
        for (Column column : table.getColumns()) {
            try {
                this.dataMap.put(column.getName(), resultSet.getObject(column.getName()));
            } catch (Exception e) {
                database.getLogger().severe("Could not get column data for table " + table.getName() + " in the database " + database.getDatabaseName());
            }
        }
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public <T extends IDataObject> T getRecord(DataObjectRegistry dataObjectRegistry, Class<T> recordClass) {
        Logger logger = database.getLogger();
        String don = recordClass.getName();
        logger.finest(String.format("Parsing data object of type %s", don));
        try {
            Table table = dataObjectRegistry.getTableByDataClass(recordClass);
            logger.finest(String.format("Table for type %s is %s", don, table.getName()));
            Constructor<?> constructor = recordClass.getDeclaredConstructor();
            logger.finest(String.format("Constructor for type %s is %s", don, constructor));
            if (constructor == null) {
                return null;
            }
            constructor.setAccessible(true);
            T record = (T) constructor.newInstance();
            logger.finest(String.format("Created a new instance of type %s", don));
            Set<Field> fields = Utils.getClassFields(recordClass);
            logger.finest(String.format("Found a total of %s fields for type %s", fields.size(), don));
            for (Field field : fields) {
                String fn = field.getName();
                field.setAccessible(true);
                logger.finest(String.format("Checking field %s of type %s", fn, don));
                ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
                if (columnInfo != null) {
                    if (columnInfo.ignored())
                        logger.finest(String.format("Field %s of type %s is ignored", fn, don));
                        continue;
                }
                
                if (DataInfo.class.isAssignableFrom(field.getType())) {
                    logger.finest(String.format("Field %s of type %s is a DataInfo", fn, don));
                    Integer id = (Integer) this.dataMap.get("id");
                    logger.finest(String.format("ID of the data object is %s", id));
                    if (field.get(record) == null) {
                        logger.finest(String.format("Field %s is null, creating a new instance", fn));
                        DataInfo dataInfo = new DataInfo();
                        dataInfo.addMapping(database.getDatabaseName(), id);
                        field.set(record, dataInfo);
                    } else {
                        record.getDataInfo().addMapping(database.getDatabaseName(), id);
                    }
                    continue;
                }
                String columnName = field.getName();
                Column column = table.getColumn(columnName);
                logger.finest(String.format("Column for field %s of type %s is %s", fn, don, column));
                Object object = null;
                Object dataObject = this.dataMap.get(field.getName());
                logger.finest(String.format("Raw value of the field %s of type %s is %s", fn, don, dataObject));
                if (dataObject != null) {
                    if (dataObject instanceof String s) {
                        if (s == null || s.equals("null") || s.equals("")) {
                            logger.finest(String.format("String value of the field %s of type %s is null or blank", fn, don));
                            continue;
                        }
                    }
                    if (column.getTypeHandler() instanceof CollectionHandler) {
                        logger.finest(String.format("Field %s of type %s is a collection", fn, don));
                        try {
                            String[] split = ((String) dataObject).split(":");
                            Class<? extends Collection> collectionType = (Class<? extends Collection>) Class.forName(split[0]);
                            logger.finest(String.format("Collection of field %s of type %s has the element type %s", fn, don, collectionType.getName()));
                            Class<?> elementType = Class.forName(split[1]);
                            Collection collection = collectionType.getDeclaredConstructor().newInstance();
                            String[] elementSplit = split[2].split(",");
                            for (String e : elementSplit) {
                                if (IDataObject.class.isAssignableFrom(elementType)) {
                                    logger.finest(String.format("Element type of field %s of type %s is an IDataObject", fn, don));
                                    int id = Integer.parseInt(e);
                                    logger.finest(String.format("ID for the element of field %s of %s is %s", fn, don, id));
                                    IDataObject colRecord = starData.getDatabaseManager().getData((Class<? extends IDataObject>) elementType, "id", id);
                                    logger.finest(String.format("Value for the element of field %s of type %s is %s", fn, don, colRecord));
                                    collection.add(colRecord);
                                } else {
                                    DataTypeHandler<?> handler = database.getTypeRegistry().getHandler(elementType);
                                    if (handler == null) {
                                        database.getLogger().severe("The field " + field.getName() + " in record " + recordClass.getName() + " is a collection and the element type does not have a DataTypeHandler");
                                        return null;
                                    }
                                    
                                    logger.finest(String.format("The element field %s of type %s is handled by %s", fn, don, handler.getClass().getName()));

                                    Object o = handler.deserialize(e);
                                    logger.finest(String.format("Element value of the collection of the field %s of type %s is %s", fn, don, o));
                                    collection.add(o);
                                }
                            }
                            object = collection;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (column.getTypeHandler() instanceof DataObjectHandler) {
                        logger.finest(String.format("Field %s of type %s is an IDataObject", fn, don));
                        String rawData = (String) this.dataMap.get(field.getName());
                        logger.finest(String.format("The raw data of field %s of type %s is %s", fn, don, rawData));
                        String[] rawSplitMain = rawData.split(":");
                        logger.finest(String.format("The main split value of field %s of type %s is %s", fn, don, Arrays.toString(rawSplitMain)));
                        String database = rawSplitMain[0].split(",")[0];
                        int id = Integer.parseInt(rawSplitMain[1].split(",")[0]);
                        object = starData.getDatabaseManager().getData(((Class<? extends IDataObject>) field.getType()), database, "id", id);
                        logger.finest(String.format("Parsed data of field %s of type %s is %s", fn, don, object));
                    } else {
                        object = column.getTypeHandler().deserialize(dataObject, field.getType());
                        logger.finest(String.format("Deserialized data of field %s of type %s is %s", fn, don, object));
                    }
                }
                if (object != null) {
                    field.set(record, object);
                }
            }
            return record;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}