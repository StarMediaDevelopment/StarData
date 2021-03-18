package com.starmediadev.data.handlers;

import com.starmediadev.data.model.DataType;
import com.starmediadev.data.model.IRecord;

public class RecordHandler extends DataTypeHandler<IRecord> {

    public RecordHandler() {
        super(IRecord.class, DataType.INT);
    }

    public Object serializeSql(Object object) {
        return object;
    }

    public IRecord deserialize(Object object) {
        return null;
    }
}
