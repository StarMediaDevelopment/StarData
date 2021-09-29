package com.starmediadev.data.model;

import com.starmediadev.data.StarData;
import com.starmediadev.data.annotations.ColumnIgnored;
import com.starmediadev.data.annotations.ColumnInfo;
import com.starmediadev.data.annotations.FieldInfo;
import com.starmediadev.data.annotations.TableInfo;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.model.source.DataSource;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import com.starmediadev.utils.Utils;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SQLDatabase {
    protected final Logger logger;

    protected final DataObjectRegistry dataObjectRegistry;
    protected final TypeRegistry typeRegistry;
    protected final StarData starData;
    protected final DataSource dataSource;
    protected final Map<String, Table> tables = new HashMap<>();
    protected final SqlProperties properties;
    
    protected String name;

    public SQLDatabase(StarData starData, SqlProperties properties, DataSource dataSource) {
        this.starData = starData;
        this.logger = starData.getLogger();
        
        this.properties = properties.clone();

        this.typeRegistry = starData.getTypeRegistry();
        this.dataObjectRegistry = starData.getDataObjectRegistry();
        this.dataSource = dataSource;
    }

    public <T extends IDataObject> List<T> getAllData(Class<T> recordType, String columnName, Object value) {
        List<T> records = new LinkedList<>();
        for (Table table : this.tables.values()) {
            String tableName = "";
            TableInfo tableInfo = recordType.getAnnotation(TableInfo.class);
            if (tableInfo != null) {
                tableName = tableInfo.tableName();
            }

            if (tableName.equals("")) {
                tableName = recordType.getSimpleName().toLowerCase();
            }
            if (table.getName().equalsIgnoreCase(tableName)) {
                String sql = Statements.SELECT.replace("{database}", name).replace("{table}", tableName);
                if (columnName != null) {
                    Column column = table.getColumn(columnName);
                    if (column == null) {
                        continue;
                    }
                    sql += " " + Statements.WHERE.replace("{column}", columnName).replace("{value}", value + "");
                }

                try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                    while (resultSet.next()) {
                        Row row = new Row(table, resultSet, this, starData);
                        records.add(row.getRecord(dataObjectRegistry, recordType));
                    }
                } catch (Exception e) {
                    logger.severe("An error occured: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        records.sort(Comparator.comparingInt(object -> object.getDataInfo().getId(name)));
        return records;
    }

    public <T extends IDataObject> T getData(Class<T> recordType, String columnName, Object value) {
        List<T> records = getAllData(recordType, columnName, value);
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    public void saveData(IDataObject record) {
        String don = record.getClass().getName();
        logger.finest("Saving a data object with the type " + don);
        Table table = dataObjectRegistry.getTableByDataClass(record.getClass());
        if (table == null) {
            logger.severe("Table for record " + record.getClass().getSimpleName() + " is null");
            return;
        }
        logger.finest(String.format("Found a table with the name %s for the type %s", table.getName(), don));
        Map<String, Object> serialized = new HashMap<>();
        Set<Field> fields = Utils.getClassFields(record.getClass());

        logger.finest(String.format("There are a total of %s fields for the type %s", fields.size(), don));

        for (Field field : fields) {
            field.setAccessible(true);
            logger.finest(String.format("Checking the field %s of the type %s", field.getName(), don));
            
            AtomicBoolean skip = new AtomicBoolean(false);

            if (DataInfo.class.isAssignableFrom(field.getType())) {
                logger.finest(String.format("The field %s has the type of DataInfo", field.getName()));
                Integer id = record.getDataInfo().getId(name);
                logger.finest(String.format("The id for this entry in the database %s is %s", name, id));
                serialized.put("id", id);
                continue;
            }

            ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
            if (columnInfo != null) {
                if (columnInfo.ignored()) {
                    logger.finest(String.format("Field %s of the type %s is ignored for database use.", field.getName(), don));
                    skip.set(true);
                }
            }

            ColumnIgnored columnIgnored = field.getAnnotation(ColumnIgnored.class);
            if (columnIgnored != null) {
                logger.finest(String.format("Field %s of the type %s is ignored for database use.", field.getName(), don));
                skip.set(true);
            }

            final Object[] fieldValue = new Object[1];
            try {
                fieldValue[0] = field.get(record);
            } catch (IllegalAccessException e) {
                logger.severe("Could not access field " + field.getName() + " in class " + record.getClass().getName() + " because " + e.getMessage());
                continue;
            }

            if (fieldValue[0] == null) {
                logger.finest(String.format("Field %s of the type %s is null, ignoring", field.getName(), don));
                continue;
            }
            
            SaveAction saveAction = null;
            if (fieldValue[0] instanceof IDataObject dataObject) {
                saveAction = () -> {
                    logger.finest(String.format("Value of the field %s of the type %s is an IDataObject, recursively saving", field.getName(), don));
                    starData.getDatabaseManager().saveData(dataObject);
                    String keys = Utils.join(dataObject.getDataInfo().getMappings().keySet(), ",");
                    String values = Utils.join(dataObject.getDataInfo().getMappings().values(), ",");
                    logger.finest("Value for the keys of the saved data object " + keys);
                    logger.finest("Value for the values of the saved data object " + values);
                    serialized.put(field.getName(), keys + ":" + values);
                };
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                saveAction = () -> {
                    logger.finest(String.format("Field %s of the type %s is a Collection", field.getName(), don));
                    Collection collection = (Collection) fieldValue[0];
                    boolean collectionContainsRecord = false;
                    List<Integer> recordIds = new ArrayList<>();
                    List<Object> serializedElements = new ArrayList<>();
                    for (Object o : collection) {
                        if (o instanceof IDataObject rec) {
                            logger.finest(String.format("Element of the collection in field %s of type %s is an IDataObject", field.getName(), don));
                            starData.getDatabaseManager().saveData(rec);
                            collectionContainsRecord = true;
                            Integer id = rec.getDataInfo().getId(name);
                            if (id == null) {
                                for (Integer value : rec.getDataInfo().getMappings().values()) {
                                    id = value;
                                    break;
                                }
                            }
                            logger.finest(String.format("ID for the data object in the collection is %s", id));
                            recordIds.add(id);
                        } else {
                            DataTypeHandler<?> handler = typeRegistry.getHandler(o.getClass());
                            if (handler == null) {
                                logger.severe("Element type " + o.getClass().getName() + " of the field " + field.getName() + " of the record " + record.getClass().getName() + " does not have a DataTypeHandler and is not a Record");
                                break;
                            }
                            logger.finest(String.format("Element of the collection in field %s of type %s is handled by the handler %s", field.getName(), don, handler.getClass().getName()));
                            serializedElements.add(handler.serializeSql(o));
                        }
                    }
                    if (collectionContainsRecord) {
                        fieldValue[0] = serializeCollection(record, field, fieldValue[0], Utils.join(recordIds, ","), serializedElements);
                    } else if (!serializedElements.isEmpty()) {
                        fieldValue[0] = serializeCollection(record, field, fieldValue[0], Utils.join(serializedElements, ","), serializedElements);
                    }
                    serialized.put(field.getName(), fieldValue[0]);
                };
            } else {
                saveAction = () -> {
                    DataTypeHandler<?> handler = table.getColumn(field.getName()).getTypeHandler();
                    if (handler == null) {
                        logger.severe("There is no DataTypeHandler for field " + field.getName() + " in class " + record.getClass().getName());
                        skip.set(true);
                        return;
                    }

                    logger.finest(String.format("Field %s of type %s is handled by %s", field.getName(), don, handler.getClass().getName()));

                    if (fieldValue[0] != null) {
                        serialized.put(field.getName(), handler.serializeSql(fieldValue[0]));
                    }  
                };
            }
            
            if (field.isAnnotationPresent(FieldInfo.class)) {
                FieldInfo fieldInfo = field.getAnnotation(FieldInfo.class);
                Class<? extends FieldHandler> handlerClass = fieldInfo.fieldHandler();
                if (Modifier.isAbstract(handlerClass.getModifiers())) {
                    logger.severe("Handler class for field " + field.getName() + " in IDataObject " + don + " is abstract.");
                    continue;
                }
                FieldHandler fieldHandler;
                try {
                    fieldHandler = handlerClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                    continue;
                }
                
                fieldHandler.onSave(field, fieldValue[0], record, serialized);
                if (fieldHandler.providesValue()) {
                    skip.set(true);
                }
            }
            
            if (!skip.get()) {
                saveAction.save();
            }
        }

        logger.finest("Finished generating information from the object.");

        DataInfo dataInfo = record.getDataInfo();

        String querySQL = null;
        Iterator<Map.Entry<String, Object>> iterator = serialized.entrySet().iterator();

        String where = Statements.WHERE.replace("{column}", "id").replace("{value}", dataInfo.getId(name) + "");
        String selectSql = Statements.SELECT.replace("{database}", this.name).replace("{table}", table.getName()) + " " + where;

        logger.finest("Checking to see if existing data exists...");
        try (Connection con = dataSource.getConnection(); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(selectSql)) {
            if (resultSet.next()) {
                Row row = new Row(table, resultSet, this, starData);
                if (!row.getDataMap().isEmpty()) {
                    StringBuilder sb = new StringBuilder();

                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        if (entry.getValue() != null) {
                            DataType type = typeRegistry.getHandler(entry.getValue().getClass()).getMysqlType();
                            if (type == null) {
                                continue;
                            }
                            sb.append(Statements.UPDATE_VALUE.replace("{column}", entry.getKey()).replace("{value}", entry.getValue() + ""));
                            if (iterator.hasNext()) {
                                sb.append(",");
                            }
                        }
                    }

                    querySQL = Statements.UPDATE.replace("{values}", sb.toString()).replace("{location}", "id=" + dataInfo.getId(name));
                    querySQL = querySQL.replace("{table}", table.getName()).replace("{database}", name);
                    logger.finest("Data exists for the id provided, using an update query");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (querySQL != null && !querySQL.equals("")) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute(querySQL);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(querySQL);
            }
        } else {
            logger.finest("No existing data is present, using an insert statement");
            StringBuilder colBuilder = new StringBuilder(), valueBuilder = new StringBuilder();
            Iterator<Column> columnIterator = table.getColumns().iterator();
            while (columnIterator.hasNext()) {
                Column column = columnIterator.next();
                if (column.isUnique() && !columnIterator.hasNext()) {
                    logger.finest("Column " + column.getName() + " is unique and there is no next values");
                    colBuilder.deleteCharAt(colBuilder.lastIndexOf(","));
                    valueBuilder.deleteCharAt(valueBuilder.lastIndexOf(","));
                    continue;
                }
                colBuilder.append("`").append(column.getName()).append("`");
                valueBuilder.append("'").append(serialized.get(column.getName())).append("'");
                if (columnIterator.hasNext()) {
                    colBuilder.append(",");
                    valueBuilder.append(",");
                }
            }

            querySQL = Statements.INSERT.replace("{columns}", colBuilder.toString()).replace("{values}", valueBuilder.toString());
            querySQL = querySQL.replace("{table}", table.getName()).replace("{database}", name);

            try (Connection con = dataSource.getConnection(); PreparedStatement statement = con.prepareStatement(querySQL, Statement.RETURN_GENERATED_KEYS)) {
                int affectedRows = statement.executeUpdate();
                logger.finest("Total affected rows for insert is " + affectedRows);
                if (affectedRows == 0) {
                    return;
                }
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generated = generatedKeys.getInt(1);
                        logger.finest("Generated id for record is " + generated);
                        record.getDataInfo().addMapping(name, generated);
                        logger.finest("DataInfo " + record.getDataInfo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(querySQL);
            }
        }
        logger.finest(String.format("Saved a data object with the type %s", don));
    }

    private Object serializeCollection(IDataObject record, Field field, Object fieldValue, String join, List<Object> serializedElements) {
        if (field.getGenericType() instanceof ParameterizedType paramType) {
            Type[] arguments = paramType.getActualTypeArguments();
            if (arguments != null && arguments.length == 1) {
                try {
                    fieldValue = field.get(record).getClass().getName() + ":" + arguments[0].getTypeName() + ":" + join;
                } catch (IllegalAccessException e) {
                }
            }
        }
        return fieldValue;
    }

    public void saveAllData(IDataObject... records) {
        if (records != null) {
            for (IDataObject record : records) {
                saveData(record);
            }
        }
    }

    public void generateTables() {
        for (Table table : this.tables.values()) {
            String sql = table.generateCreationStatement(this.name);

            try (Connection con = dataSource.getConnection(); Statement statement = con.createStatement()) {
                statement.execute(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (Connection con = dataSource.getConnection()) {
                DatabaseMetaData databaseMeta = con.getMetaData();
                try (ResultSet columns = databaseMeta.getColumns(null, null, table.getName(), null)) {
                    List<String> existingColumns = new ArrayList<>();
                    while (columns.next()) {
                        String name = columns.getString("COLUMN_NAME");
                        existingColumns.add(name);
                    }

                    List<String> columnSqls = new ArrayList<>();
                    for (Column column : table.getColumns()) {
                        if (!existingColumns.contains(column.getName())) {
                            String columnType;
                            DataTypeHandler<?> handler = column.getTypeHandler();
                            if (handler.getMysqlType().equals(DataType.VARCHAR)) {
                                int length = handler.getDefaultLength();
                                if (column.getLength() > 0) {
                                    length = column.getLength();
                                }
                                columnType = handler.getMysqlType().name() + "(" + length + ")";
                            } else {
                                columnType = handler.getMysqlType().name();
                            }
                            String columnSql = Statements.ALTER_TABLE.replace("{table}", table.getName()).replace("{database}", name).replace("{logic}", Statements.ADD_COLUMN.replace("{column}", column.getName()).replace("{type}", columnType));
                            columnSqls.add(columnSql);
                        }
                        existingColumns.remove(column.getName());
                    }

                    if (!existingColumns.isEmpty()) {
                        for (String existingColumn : existingColumns) {
                            String columnSql = Statements.ALTER_TABLE.replace("{table}", table.getName()).replace("{database}", name).replace("{logic}", Statements.DROP_COLUMN.replace("{column}", existingColumn));
                            columnSqls.add(columnSql);
                        }
                    }

                    if (!columnSqls.isEmpty()) {
                        for (String columnSql : columnSqls) {
                            try (Statement statement = con.createStatement()) {
                                statement.executeUpdate(columnSql);
                            } catch (Exception e) {
                                if (!e.getMessage().contains("Can't DROP")) {
                                    System.out.println(columnSql);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (!e.getMessage().contains("Can't DROP")) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addTable(Table table) {
        if (table != null) {
            this.tables.put(table.getName().toLowerCase(), table);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public void deleteData(IDataObject object) {
        Table table = dataObjectRegistry.getTableByDataClass(object.getClass());
        if (table == null) {
            logger.severe("A table for the class " + object.getClass().getName() + " has not been registered.");
            return;
        }

        String where = Statements.WHERE.replace("{column}", "id").replace("{value}", object.getDataInfo().getId(name) + "");
        String deleteSql = Statements.DELETE.replace("{database}", this.name).replace("{table}", table.getName());
        try (Connection con = dataSource.getConnection(); Statement statement = con.createStatement()) {
            statement.execute(deleteSql + " " + where);
        } catch (Exception e) {

        }
    }

    public String getName() {
        return name;
    }
}
