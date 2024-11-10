package com.example.ad_gk.model;

public class Certificate {

    private String name;
    private String year;

    public Certificate(String name, String year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }
}
