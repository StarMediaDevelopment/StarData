package com.starmediadev.sql.model;

import com.starmediadev.sql.StarSQL;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDataSource {
    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;
    
    public MysqlDataSource(String url, String username, String password) {
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(250);
        config.setMaximumPoolSize(50);
        config.setDriverClassName(StarSQL.DRIVER_CLASS);
        ds = new HikariDataSource(config);
    }
    
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
