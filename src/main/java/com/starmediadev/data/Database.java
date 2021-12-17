package com.starmediadev.data;

/**
 * This class is for saving data into a database of some sort. 
 * The plan is to have MySQL, SQLite, JSON and YAML
 * MySQL and SQLite were already previously done, so there is a basis for this part 
 * For Redis pretty much the same thing for the SQL stuff, I had done something previously for HungerMania
 * For JSON based stuff, this was done previously as well and has some stuff in the StarLib project for JSON, but that needs to be rewritten
 * For YAML based stuff, Just going to copy a lot of the config stuff from Spigot and change things accordinly
 * 
 * All of these types will also have the ability to save a Document as a configuration instead of as data 
 */
public abstract class Database {
    
    /**
     * This is the ID of the Database which consists of the type and name of the database
     */
    protected String id;
    
    /**
     * The ID of the database
     * @return The ID of the Database
     */
    public String getId() {
        return id;
    }
}