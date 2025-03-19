package com.example.demo.models;

public class DataMapping {
    private KeyMapping key;
    private DataSource fromDataSource;
    private String into;

    public KeyMapping getKey() { return key; }
    public void setKey(KeyMapping key) { this.key = key; }

    public DataSource getFromDataSource() { return fromDataSource; }
    public void setFromDataSource(DataSource fromDataSource) { this.fromDataSource = fromDataSource; }

    public String getInto() { return into; }
    public void setInto(String into) { this.into = into; }
}
