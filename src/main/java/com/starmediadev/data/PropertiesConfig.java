package com.starmediadev.data;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * An implementation from {@link Config} for {@link java.util.Properties} files
 */
public class PropertiesConfig extends Config {
    
    private final Properties properties = new Properties();
    protected String description;
    
    public PropertiesConfig(Path path, String description) {
        super(path);
        this.description = description;
    }
    
    public boolean load() throws Exception {
        InputStream inputStream = Files.newInputStream(path);
        properties.load(inputStream);
        return true;
    }
    
    public boolean save() throws Exception {
        properties.store(Files.newOutputStream(path), description);
        return false;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
