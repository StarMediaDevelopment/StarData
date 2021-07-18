package com.starmediadev.data.model;

public final class DataInfo {
    private int id; //Like the deprecated field in IDataObject this is the entry id from the database
    private String name; //This is the name of the database that the object was saved to.

    public DataInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
