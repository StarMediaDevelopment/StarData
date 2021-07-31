package com.starmediadev.data.model.source;

import java.sql.Connection;

public interface DataSource {
    
    Connection getConnection();
}
