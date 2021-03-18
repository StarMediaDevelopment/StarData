package com.starmediadev.sql.handlers;

import com.starmediadev.sql.model.DataType;
import com.starmediadev.sql.model.IRecord;

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
