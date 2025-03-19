package com.example.demo;

public class CSVDataSource {
    private String path;
    private String keyColumn;
    private String delimiter;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getKeyColumn() { return keyColumn; }
    public void setKeyColumn(String keyColumn) { this.keyColumn = keyColumn; }

    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }
}
