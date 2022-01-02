package com.starmediadev.data;

/**
 * This is the primary entry point to this library. Everthing that can be saved will be of this type or something that extends this type.
 */
public interface IDocument {
    /**
     * This ID is going to be based on an Internal System for all of the database types registered to the instance of the library
     * This is to keep it very simple on the developer end of this library
     * @return The ID of the Document
     */
    long getId();
    
    /**
     * This sets the ID for this document.
     * The library will be checking the id to make sure, it will throw an error and skip if it doesn't
     */
    void setId();
}