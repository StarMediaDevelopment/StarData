package com.starmediadev.plugins.data;

import com.starmediadev.data.model.DataInfo;
import com.starmediadev.data.model.IDataObject;

public class TestTwo implements IDataObject {

    private DataInfo dataInfo = new DataInfo();
    
    private String code;

    private TestTwo() { }

    public TestTwo(String code) {
        this.code = code;
    }

    public DataInfo getDataInfo() {
        return dataInfo;
    }

    public String toString() {
        return "TestTwo{" +
                "dataInfo=" + dataInfo +
                ", code='" + code + '\'' +
                '}';
    }
}
