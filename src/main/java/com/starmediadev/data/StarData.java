package com.starmediadev.data;

import com.starmediadev.utils.helper.FileHelper;

import java.nio.file.Files;
import java.nio.file.Path;

public class StarData {
    private static Path DATA_FOLDER;
    
    //Config files
    private static PropertiesConfig documentsConfig;
    
    /**
     * Sets the current data folder for storing library related information
     * @param dataFolder The folder location as a {@link Path}
     * @throws Exception Exceptions from checking and as well as moving existing files
     */
    public static void setDataFolder(Path dataFolder) throws Exception {
        if (dataFolder == null) {
            throw new NullPointerException("Data folder cannot be null.");
        }
        
        if (!Files.isDirectory(dataFolder)) {
            throw new IllegalArgumentException("Provided path is not a directory.");
        }
        
       if (documentsConfig != null) {
           documentsConfig.move(dataFolder);
       } else {
           documentsConfig = new PropertiesConfig(FileHelper.subPath(dataFolder, "documents.txt"), "StarData documents properties file.");
       }
       
       StarData.DATA_FOLDER = dataFolder;
    }
    
    /**
     * Gets the current data folder for the library
     * @return The current data folder of the library
     */
    public static Path getDataFolder() {
        return StarData.DATA_FOLDER;
    }
    
    public static PropertiesConfig getDocumentsConfig() {
        return documentsConfig;
    }
}