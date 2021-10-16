package com.starmediadev.data.model.source;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDataSource implements DataSource {
    
    private Path fileLocation;

    public SqliteDataSource(Path fileLocation) {
        this.fileLocation = fileLocation;
    }

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
