package com.starmediadev.data.internal.objects;

/*
Replace this with a builder or appendable type builder. 
Main class will be called SQLStatement or something, or query 
This will just be a base class with a few abstract methods as no one statement is similar and a toSql() method that will build the actual statement

Then each statement will have a subclass with a builder pattern that will have settings for each thing
For example, the create table statement will have a field for the database, table and columns and when the toSql() method is called it will transform it into the actual statement
 */
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
