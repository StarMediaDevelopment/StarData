package com.starmediadev.data.model;

import com.starmediadev.data.StarData;
import com.starmediadev.data.annotations.ColumnInfo;
import com.starmediadev.data.annotations.TableInfo;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import com.starmediadev.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class MysqlDatabase {
    private final Logger logger;

    private final DataObjectRegistry dataObjectRegistry;
    private final TypeRegistry typeRegistry;

    private Queue<IDataObject> queue = new ArrayBlockingQueue<>(100); //TODO

    private final Map<String, Table> tables = new HashMap<>();
    private final String databaseName, host;

    public MysqlDatabase(StarData starData, SqlProperties properties) {
        this.logger = starData.getLogger();
        this.databaseName = properties.getDatabase();
        this.host = properties.getHost();

        this.typeRegistry = starData.getTypeRegistry();
        this.dataObjectRegistry = starData.getDataObjectRegistry();
    }

    public <T extends IDataObject> List<T> getAllData(MysqlDataSource dataSource, Class<T> recordType, String columnName, Object value) {
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
                String sql = "SELECT * FROM " + table.getName();
                if (columnName != null) {
                    Column column = table.getColumn(columnName);
                    if (column == null) {
                        continue;
                    }
                    sql += " WHERE `" + column.getName() + "` = '" + value + "'";
                }

                try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                    while (resultSet.next()) {
                        Row row = new Row(table, resultSet, this, dataSource);
                        records.add(row.getRecord(dataObjectRegistry, recordType));
                    }
                } catch (Exception e) {
                    logger.severe("An error occured: " + e.getMessage());
                }
            }
        }

        records.sort(Comparator.comparingInt(object -> object.getDataInfo().getId(databaseName)));
        return records;
    }

    public <T extends IDataObject> T getData(MysqlDataSource dataSource, Class<T> recordType, String columnName, Object value) {
        List<T> records = getAllData(dataSource, recordType, columnName, value);
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    public void saveData(MysqlDataSource dataSource, IDataObject record) {
        Table table = dataObjectRegistry.getTableByDataClass(record.getClass());
        if (table == null) {
            System.out.println("Table for record " + record.getClass().getSimpleName() + " is null");
            return;
        }
        Map<String, Object> serialized = new HashMap<>();
        Set<Field> fields = Utils.getClassFields(record.getClass());

        for (Field field : fields) {
            field.setAccessible(true);

            if (DataInfo.class.isAssignableFrom(field.getType())) {
                serialized.put("id", record.getDataInfo().getId(databaseName));
                continue;
            }

            ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
            if (columnInfo != null) {
                if (columnInfo.ignored()) {
                    continue;
                }
            }

            Object fieldValue;
            try {
                fieldValue = field.get(record);
            } catch (IllegalAccessException e) {
                logger.severe("Could not access field " + field.getName() + " in class " + record.getClass().getName() + " because " + e.getMessage());
                continue;
            }

            if (fieldValue == null) {
                continue;
            }

            if (fieldValue instanceof IDataObject dataObject) {
                saveData(dataSource, dataObject);
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                Collection collection = (Collection) fieldValue;
                boolean collectionContainsRecord = false;
                List<Integer> recordIds = new ArrayList<>();
                List<Object> serializedElements = new ArrayList<>();
                for (Object o : collection) {
                    if (o instanceof IDataObject rec) {
                        saveData(dataSource, rec);
                        collectionContainsRecord = true;
                        recordIds.add(rec.getDataInfo().getId(databaseName));
                    } else {
                        DataTypeHandler<?> handler = typeRegistry.getHandler(o.getClass());
                        if (handler == null) {
                            logger.severe("Element type " + o.getClass().getName() + " of the field " + field.getName() + " of the record " + record.getClass().getName() + " does not have a DataTypeHandler and is not a Record");
                            break;
                        }

                        serializedElements.add(handler.serializeSql(o));
                    }
                }
                if (collectionContainsRecord) {
                    fieldValue = serializeCollection(dataSource, record, field, fieldValue, Utils.join(recordIds, ","), serializedElements);
                } else if (!serializedElements.isEmpty()) {
                    fieldValue = serializeCollection(dataSource, record, field, fieldValue, Utils.join(serializedElements, ","), serializedElements);
                }
            }

            DataTypeHandler<?> handler = table.getColumn(field.getName()).getTypeHandler();
            if (handler == null) {
                logger.severe("There is no DataTypeHandler for field " + field.getName() + " in class " + record.getClass().getName());
                continue;
            }

            if (fieldValue != null) {
                serialized.put(field.getName(), handler.serializeSql(fieldValue));
            }
        }

        DataInfo dataInfo = record.getDataInfo();

        String querySQL = null;
        Iterator<Map.Entry<String, Object>> iterator = serialized.entrySet().iterator();

        String where = Statements.WHERE.replace("{column}", "id").replace("{value}", dataInfo.getId(databaseName) + "");
        String selectSql = Statements.SELECT.replace("{database}", this.databaseName).replace("{table}", table.getName()) + " " + where;

        try (Connection con = dataSource.getConnection(); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(selectSql)) {
            if (resultSet.next()) {
                Row row = new Row(table, resultSet, this, dataSource);
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

                    querySQL = Statements.UPDATE.replace("{values}", sb.toString()).replace("{location}", "id=" + dataInfo.getId(databaseName));
                    querySQL = querySQL.replace("{table}", table.getName()).replace("{database}", databaseName);
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
        }

        if (querySQL == null || querySQL.equals("")) {
            StringBuilder colBuilder = new StringBuilder(), valueBuilder = new StringBuilder();
            Iterator<Column> columnIterator = table.getColumns().iterator();
            while (columnIterator.hasNext()) {
                Column column = columnIterator.next();
                if (column.isUnique() && !columnIterator.hasNext()) {
                    System.out.println("Column " + column.getName() + " is unique and there is no next values");
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
            querySQL = querySQL.replace("{table}", table.getName()).replace("{database}", databaseName);

            try (Connection con = dataSource.getConnection(); PreparedStatement statement = con.prepareStatement(querySQL, Statement.RETURN_GENERATED_KEYS)) {
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    return;
                }
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.getDataInfo().addMapping(databaseName, generatedKeys.getInt(1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(querySQL);
            }
        }
    }

    private Object serializeCollection(MysqlDataSource dataSource, IDataObject record, Field field, Object fieldValue, String join, List<Object> serializedElements) {
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

    public void saveAllData(MysqlDataSource dataSource, IDataObject... records) {
        if (records != null) {
            for (IDataObject record : records) {
                saveData(dataSource, record);
            }
        }
    }

    public void generateTables(MysqlDataSource dataSource) {
        for (Table table : this.tables.values()) {
            String sql = table.generateCreationStatement(this.databaseName);

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
                            String columnSql = Statements.ALTER_TABLE.replace("{table}", table.getName()).replace("{database}", databaseName).replace("{logic}", Statements.ADD_COLUMN.replace("{column}", column.getName()).replace("{type}", columnType));
                            columnSqls.add(columnSql);
                        }
                        existingColumns.remove(column.getName());
                    }

                    if (!existingColumns.isEmpty()) {
                        for (String existingColumn : existingColumns) {
                            String columnSql = Statements.ALTER_TABLE.replace("{table}", table.getName()).replace("{database}", databaseName).replace("{logic}", Statements.DROP_COLUMN.replace("{column}", existingColumn));
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

    public String getDatabaseName() {
        return databaseName;
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public void deleteData(MysqlDataSource dataSource, IDataObject object) {
        Table table = dataObjectRegistry.getTableByDataClass(object.getClass());
        if (table == null) {
            logger.severe("A table for the class " + object.getClass().getName() + " has not been registered.");
            return;
        }

        String where = Statements.WHERE.replace("{column}", "id").replace("{value}", object.getDataInfo().getId(databaseName) + "");
        String deleteSql = Statements.DELETE.replace("{database}", this.databaseName).replace("{table}", table.getName());
        try (Connection con = dataSource.getConnection(); Statement statement = con.createStatement()) {
            statement.execute(deleteSql + " " + where);
        } catch (Exception e) {

        }
    }

    public String getHost() {
        return host;
    }
}