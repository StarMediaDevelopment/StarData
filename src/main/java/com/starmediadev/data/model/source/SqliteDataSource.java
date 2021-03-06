package com.starmediadev.data.model.source;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public record SqliteDataSource(Path fileLocation) implements DataSource {
    
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + fileLocation.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getName() {
        return fileLocation.getFileName().toString().substring(0, fileLocation.getFileName().toString().lastIndexOf('.'));
    }
}
