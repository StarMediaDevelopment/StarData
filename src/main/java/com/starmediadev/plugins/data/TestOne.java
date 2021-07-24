package com.starmediadev.plugins.data;

import com.starmediadev.data.model.DataInfo;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.utils.Code;

import java.util.ArrayList;
import java.util.List;

public class TestOne implements IDataObject {
    
    private DataInfo dataInfo = new DataInfo();
    
    private String code;
    private List<TestTwo> testTwoList = new ArrayList<>();
    
    private TestOne() {}

    public TestOne(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void generateTestTwo() {
        this.testTwoList.add(new TestTwo(new Code(16).toString()));
    }

    public DataInfo getDataInfo() {
        return dataInfo;
    }

    public String toString() {
        return "TestOne{" +
                "dataInfo=" + dataInfo +
                ", code='" + code + '\'' +
                ", testTwoList=" + testTwoList +
                '}';
    }
}
