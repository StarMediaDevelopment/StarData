package com.starmediadev.data.properties;

import java.nio.file.Path;

public class SqliteProperties extends SqlProperties {
    
    private Path file;
    
    public SqliteProperties() {}
    
    private SqliteProperties(SqliteProperties properties) {
        setFile(properties.getFile());
    }
    
    public SqliteProperties setFile(Path file) {
        this.file = file;
        return this;
    }

    public Path getFile() {
        return file;
    }

    public String toJDBCUrl() {
        return "jdbc:sqlite:" + file.toString();
    }

    public SqlProperties clone() {
        return new SqliteProperties(this);
    }
}
