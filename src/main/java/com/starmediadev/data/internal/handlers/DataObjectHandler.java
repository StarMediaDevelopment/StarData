package com.starmediadev.data.internal.handlers;

import com.starmediadev.data.model.DataTypeHandler;
import com.starmediadev.data.model.DataType;
import com.starmediadev.data.model.IDataObject;

public class DataObjectHandler extends DataTypeHandler<IDataObject> {

    public DataObjectHandler() {
        super(IDataObject.class, DataType.INT);
    }

    public Object serializeSql(Object object) {
        return object;
    }

    public IDataObject deserialize(Object object) {
        return null;
    }
}
