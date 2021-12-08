package com.starmediadev.data;

public interface IDocument {
    /**
     * This ID is going to be based on an Internal System for all of the database types registered to the instance of the library
     * This is to keep it very simple on the developer end of this library
     * @return The ID of the Document
     */
    long getId(); 
}