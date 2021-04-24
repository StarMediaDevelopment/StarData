package com.starmediadev.data.model;

public interface MultiTableRecord extends IRecord {
    
    void setTableName(String tableName);
    String getTableName();
}
