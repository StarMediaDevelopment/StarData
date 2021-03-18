package com.starmediadev.data.model;

import com.starmediadev.data.handlers.DataTypeHandler;

public class Column {
    protected String name;
    protected DataTypeHandler<?> typeHandler;
    protected int length;
    protected boolean autoIncrement, unique;

    public Column(String name, DataTypeHandler<?> typeHandler, boolean autoIncrement, boolean unique) {
        this(name, typeHandler, 0, autoIncrement, unique);
    }

    public Column(String name, DataTypeHandler<?> typeHandler, int length, boolean autoIncrement, boolean unique) {
        this.name = name;
        this.typeHandler = typeHandler;
        this.length = length;
        this.autoIncrement = autoIncrement;
        this.unique = unique;
    }

    public Column(String name, DataTypeHandler<?> typeHandler) {
        this(name, typeHandler, false, false);
    }

    public Column(String name, DataTypeHandler<?> typeHandler, int length) {
        this(name, typeHandler, length, false, false);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public String getCreationString() {
        StringBuilder sb = new StringBuilder("`");
        sb.append(name).append("` ").append(this.typeHandler.getMysqlType().name());
        int columnLength = this.typeHandler.getDefaultLength();
        if (this.length > 0) {
            columnLength = length;
        }
        if (columnLength > 0) {
            sb.append("(").append(columnLength).append(")");
        }

        if (unique) {
            sb.append(" ").append("NOT NULL");
        }

        if (autoIncrement) {
            sb.append(" ").append("AUTO_INCREMENT");
        }

        if (unique) {
            sb.append(", PRIMARY KEY (`").append(this.name).append("`)");
        }

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public DataTypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isUnique() {
        return unique;
    }
}