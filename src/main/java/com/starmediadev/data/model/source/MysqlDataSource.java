package com.starmediadev.data.model.source;

import com.starmediadev.data.StarData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDataSource implements DataSource {
    private final HikariConfig config = new HikariConfig();
    private final HikariDataSource ds;
    
    public MysqlDataSource(String url, String username, String password) {
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(250);
        config.setMaximumPoolSize(50);
        config.setDriverClassName(StarData.mysqlDriverClass);
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
