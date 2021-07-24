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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        try {
            Table table = dataObjectRegistry.getTableByDataClass(recordClass);
            Constructor<?> constructor = recordClass.getDeclaredConstructor();
            if (constructor == null) {
                return null;
            }
            constructor.setAccessible(true);
            T record = (T) constructor.newInstance();
            Set<Field> fields = Utils.getClassFields(recordClass);
            for (Field field : fields) {
                field.setAccessible(true);
                ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
                if (columnInfo != null) {
                    if (columnInfo.ignored())
                        continue;
                }
                
                if (DataInfo.class.isAssignableFrom(field.getType())) {
                    if (field.get(record) == null) {
                        DataInfo dataInfo = new DataInfo();
                        dataInfo.addMapping(database.getDatabaseName(), (Integer) this.dataMap.get("id"));
                        field.set(record, dataInfo);
                    } else {
                        record.getDataInfo().addMapping(database.getDatabaseName(), (Integer) this.dataMap.get("id"));
                    }
                    continue;
                }
                String columnName = field.getName();
                Column column = table.getColumn(columnName);
                Object object = null;
                Object dataObject = this.dataMap.get(field.getName());
                if (dataObject != null) {
                    if (dataObject instanceof String s) {
                        if (s == null || s.equals("null") || s.equals("")) {
                            continue;
                        }
                    }
                    if (column.getTypeHandler() instanceof CollectionHandler) {
                        try {
                            String[] split = ((String) dataObject).split(":");
                            Class<? extends Collection> collectionType = (Class<? extends Collection>) Class.forName(split[0]);
                            Class<?> elementType = Class.forName(split[1]);
                            Collection collection = collectionType.getDeclaredConstructor().newInstance();
                            String[] elementSplit = split[2].split(",");
                            for (String e : elementSplit) {
                                if (IDataObject.class.isAssignableFrom(elementType)) {
                                    int id = Integer.parseInt(e);
                                    IDataObject colRecord = starData.getDatabaseManager().getData((Class<? extends IDataObject>) elementType, "id", id);
                                    collection.add(colRecord);
                                } else {
                                    DataTypeHandler<?> handler = database.getTypeRegistry().getHandler(elementType);
                                    if (handler == null) {
                                        database.getLogger().severe("The field " + field.getName() + " in record " + recordClass.getName() + " is a collection and the element type does not have a DataTypeHandler");
                                        return null;
                                    }

                                    Object o = handler.deserialize(e);
                                    collection.add(o);
                                }
                            }
                            object = collection;
                        } catch (Exception e) {
                        }
                    } else if (column.getTypeHandler() instanceof DataObjectHandler) {
                        String rawData = (String) this.dataMap.get(field.getName());
                        String[] rawSplitMain = rawData.split(":");
                        String database = rawSplitMain[0].split(",")[0];
                        int id = Integer.parseInt(rawSplitMain[1].split(",")[0]);
                        object = starData.getDatabaseManager().getData(((Class<? extends IDataObject>) field.getType()), "id", id);
                    } else {
                        object = column.getTypeHandler().deserialize(dataObject, field.getType());
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