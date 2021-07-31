package com.starmediadev.data.properties;

import java.util.Properties;

public abstract class SqlProperties implements Cloneable {
    protected Properties properties;

    public SqlProperties() {
        this.properties = new Properties();
    }

    public abstract String toJDBCUrl();
    public abstract SqlProperties clone();

    public Properties getConnectionProperties() {
        return properties;
    }
}
