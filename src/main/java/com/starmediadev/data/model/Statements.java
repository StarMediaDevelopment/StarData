package com.starmediadev.data.model;

public final class Statements {
    private Statements() {}
    
    public static final String CREATE_DATABASE = "CREATE DATABASE {name};";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `{database}`.`{table}` ({columns}) ENGINE=InnoDB;";
    public static final String COLUMN_FORMAT = "`{colName}` {colType}";
    public static final String INSERT = "INSERT INTO `{database}`.`{table}` ({columns}) VALUES ({values});";
    public static final String UPDATE = "UPDATE `{database}`.`{table}` SET {values} WHERE {location}";
    public static final String UPDATE_VALUE = "`{column}`='{value}'";
    public static final String SELECT = "SELECT * FROM `{database}`.`{table}`";
    public static final String WHERE = "WHERE `{column}`='{value}'";
    public static final String DELETE = "DELETE FROM `{database}`.`{table}`";
    
    public static final String ALTER_TABLE = "ALTER TABLE `{database}`.`{table}` {logic};";
    public static final String ADD_COLUMN = "ADD COLUMN `{column}` {type}";
    public static final String DROP_COLUMN = "DROP COLUMN `{column}`";
    
    public static final String PRIMARY_COL = "`id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)";
}
