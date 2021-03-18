package com.starmediadev.sql.properties;

import java.io.*;
import java.util.Properties;

public class SqlProperties {
    private Properties properties;
    
    public SqlProperties() {}
    
    public void load(File file) {
        properties = new Properties();
        try {
            FileInputStream in = new FileInputStream(file);
            properties.load(in);
            in.close();
        } catch (IOException e) {}
        
        load(properties);
    }
    
    public void load(Properties properties) {
        this.properties = properties;
    }
    
    public static Properties createDefaultProperties() {
        Properties properties = new Properties();
        properties.put("mysql-host", "localhost");
        properties.put("mysql-database", "database");
        properties.put("mysql-username", "username");
        properties.put("mysql-password", "password");
        properties.put("mysql-port", "3306");
        return properties;
    }

    public String getUsername() {
        return properties.getProperty("mysql-username");
    }

    public String getHost() {
        return properties.getProperty("mysql-host");
    }

    public String getDatabase() {
        return properties.getProperty("mysql-database");
    }

    public String getPassword() {
        return properties.getProperty("mysql-password");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("mysql-port"));
    }

    public void setDatabase(String database) {
        this.properties.put("mysql-database", database);
    }
    
    public void setHost(String host) {
        this.properties.put("mysql-host", host);
    }
    
    public void setUsername(String username) {
        this.properties.put("mysql-username", username);
    }
    
    public void setPassword(String password) {
        this.properties.put("mysql-password", password);
    }
    
    public void setPort(int port) {
        this.properties.put("mysql-port", port + "");
    }
    
    public void save(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            properties.store(out, "");
            out.close();
        } catch (IOException e) {}
    }

    public Properties getConnectionProperties() {
        return properties;
    }
}
