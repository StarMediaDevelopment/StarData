package com.starmediadev.data.model;

public interface MultiTableDataObject extends IDataObject {
    
    void setTableName(String tableName);
    String getTableName();
}
