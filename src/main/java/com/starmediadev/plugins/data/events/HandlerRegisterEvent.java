package com.starmediadev.plugins.data.events;

import com.starmediadev.data.StarData;
import com.starmediadev.data.model.DataTypeHandler;

import java.util.HashSet;
import java.util.Set;

public class HandlerRegisterEvent extends StarDataEvent {
    
    private Set<DataTypeHandler<?>> typeHandlers = new HashSet<>();
    
    public HandlerRegisterEvent(StarData starData) {
        super(starData);
    }

    public Set<DataTypeHandler<?>> getTypeHandlers() {
        return typeHandlers;
    }
    
    public void registerTypeHandler(DataTypeHandler<?> typeHandler) {
        this.typeHandlers.add(typeHandler);
    }
}
