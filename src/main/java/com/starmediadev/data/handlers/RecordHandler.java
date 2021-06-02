package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;
import com.starmediadev.data.model.IDataObject;

public class RecordHandler extends DataTypeHandler<IDataObject> {

    public RecordHandler() {
        super(IDataObject.class, DataType.INT);
    }

    public Object serializeSql(Object object) {
        return object;
    }

    public IDataObject deserialize(Object object) {
        return null;
    }
}
