package com.starmediadev.data.model;

public class AbstractDataObject implements IDataObject {
    protected DataInfo dataInfo = new DataInfo();

    public DataInfo getDataInfo() {
        return dataInfo;
    }
}
