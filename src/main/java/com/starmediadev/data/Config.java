package com.starmediadev.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is for general configuration. It can be used for pretty much anything
 */
public abstract class Config {
    protected Path path;
    
    public Config(Path path) {
        this.path = path;
    }
    
    /**
     * Loads data into the config based on the implementation
     * @return Returns if the loading was successful or not. If the loading was not successful due to an error, that error is provided instead.
     * @throws Exception General exception for loading issues like {@link java.io.IOException} or {@link java.sql.SQLException}
     */
    public abstract boolean load() throws Exception;
    
    /**
     * Saves the config contents.
     * @return Returns if the saving was successful or not. If the saving was not successful due to an error, that error is provided instead.
     * @throws Exception General exception for saving issues like {@link java.io.IOException} or {@link java.sql.SQLException}
     */
    public abstract boolean save() throws Exception;
    
    /**
     * Copies the file to a new location.
     * @param dest The destination of where the copy should be located
     * @throws IOException The exception thrown from {@linkjava.nio.Files#copy()}
     */
    public final void copy(Path dest) throws IOException {
        Files.copy(path, dest);
    }
    
    /**
     * Moves the file to a new location and changes the pointer field
     * @param dest The new destination of this config
     * @throws IOException The exception thrown from {@linkjava.nio.Files#copy()}
     */
    public final void move(Path dest) throws IOException {
        Files.move(path, dest);
        this.path = dest;
    }
    
    public Path getPath() {
        return path;
    }
}