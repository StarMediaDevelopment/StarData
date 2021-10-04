package com.starmediadev.data.model;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Must have a default constructor and all functionality is handled in the interface methods. The classes are reflectively created.
 */
public interface FieldHandler {

    /**
     * The behavior of how data is processed requires this method.
     * If this handler sets a value for the field, this method must return true. 
     * If this handler processes the data, but is not either saved to the database or is otherwise handled but not loaded into the field, this method should return false.
     * @return If this handler provides a value.
     */
    boolean providesValue();

    /**
     * This method is for when this field is being processed for being saved to the database.
     */
    void onSave(Field field, Object fieldValue, Object parent, Map<String, Object> serialized);

    /**
     * This method is for when the field is being processed for being loaded from the database into an object.
     */
    Object onLoad( Field field, Object value, Object parent);
}
