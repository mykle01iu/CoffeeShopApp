package com.example.myapplication.model;

public class Category {
    public final int id;
    public String name;
    public String description;

    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
