package com.starmediadev.plugins.data;

import com.starmediadev.data.model.DataInfo;
import com.starmediadev.data.model.IDataObject;

public class TestThree implements IDataObject {

    private DataInfo dataInfo = new DataInfo();
    
    private String code;

    private TestThree() { }

    public TestThree(String code) {
        this.code = code;
    }

    public DataInfo getDataInfo() {
        return dataInfo;
    }

    public String toString() {
        return "TestThree{" +
                "dataInfo=" + dataInfo +
                ", code='" + code + '\'' +
                '}';
    }
}
