package com.starmediadev.sql.model;

import com.starmediadev.sql.annotations.ColumnInfo;
import com.starmediadev.sql.handlers.CollectionHandler;
import com.starmediadev.sql.handlers.DataTypeHandler;
import com.starmediadev.sql.handlers.RecordHandler;
import com.starmediadev.sql.registries.RecordRegistry;
import com.starmediadev.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Row {
    private final MysqlDatabase database;
    protected Map<String, Object> dataMap = new HashMap<>();
    protected Table table;

    public Row(Table table, ResultSet resultSet, MysqlDatabase database) {
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

    public <T extends IRecord> T getRecord(RecordRegistry recordRegistry, Class<T> recordClass) {
        try {
            Table table = recordRegistry.getTableByRecordClass(recordClass);
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
                String columnName = field.getName();
                Column column = table.getColumn(columnName);
                Object object = null;
                Object dataObject = this.dataMap.get(field.getName());
                if (column.getTypeHandler() instanceof CollectionHandler) {
                    try {
                        String[] split = ((String) dataObject).split(":");
                        Class<? extends Collection> collectionType = (Class<? extends Collection>) Class.forName(split[0]);
                        Class<?> elementType = Class.forName(split[1]);
                        Collection collection = collectionType.getDeclaredConstructor().newInstance();
                        String[] elementSplit = split[2].split(",");
                        for (String e : elementSplit) {
                            if (IRecord.class.isAssignableFrom(elementType)) {
                                int id = Integer.parseInt(e);
                                IRecord colRecord = database.getRecord(recordRegistry, (Class<? extends IRecord>) elementType, "id", id);
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
                    } catch (Exception e) {}
                } else if (column.getTypeHandler() instanceof RecordHandler) {
                    object = database.getRecord(recordRegistry, ((Class<? extends IRecord>) field.getType()), "id", this.dataMap.get(field.getName())); //TODO
                } else {
                    object = column.getTypeHandler().deserialize(dataObject, field.getType());
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