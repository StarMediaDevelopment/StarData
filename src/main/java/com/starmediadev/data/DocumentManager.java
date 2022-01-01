package com.starmediadev.data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This manages the documents for the library. Documents are what every method of saving uses within this library
 */
public class DocumentManager {
    /**
     * This is the control variable for the Document ID system. This will have to be stored somewhere
     */
    protected static final AtomicLong CURRENT_ID = new AtomicLong(0);
    
    /**
     * This gets a new ID and increments the current ID as well
     * @return The new ID to use
     */
    public static synchronized long getId() {
        return CURRENT_ID.getAndIncrement();
    }
}
