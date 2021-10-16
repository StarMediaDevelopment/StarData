package com.starmediadev.data.properties;

public class MysqlProperties extends SqlProperties {
    
    private MysqlProperties(MysqlProperties properties) {
        setUsername(properties.getUsername());
        setDatabase(properties.getDatabase());
        setHost(properties.getHost());
        setPort(properties.getPort());
        setPassword(properties.getPassword());
    }
    
    public MysqlProperties() {
        
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

    public MysqlProperties setDatabase(String database) {
        if (database != null)
            this.properties.put("mysql-database", database);
        return this;
    }

    public MysqlProperties setHost(String host) {
        if (host != null)
            this.properties.put("mysql-host", host);
        return this;
    }

    public MysqlProperties setUsername(String username) {
        if (username != null)
            this.properties.put("mysql-username", username);
        return this;
    }

    public MysqlProperties setPassword(String password) {
        if (password != null)
            this.properties.put("mysql-password", password);
        return this;
    }

    public MysqlProperties setPort(int port) {
        if (port != 0)
            this.properties.put("mysql-port", port + "");
        return this;
    }

    public String toJDBCUrl() {
        String url = "jdbc:mysql://" + getHost() + ":" + getPort();
        if (getDatabase() != null) {
            url += "/" + getDatabase();
        }
        return url;
    }

    public SqlProperties clone() {
        return new MysqlProperties(this);
    }
}
