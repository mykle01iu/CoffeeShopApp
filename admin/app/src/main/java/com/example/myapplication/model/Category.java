package com.example.myapplication.model;

public class Category {
    public final int id;
    public String code;
    public String name;
    public String description;
    public boolean available;

    public Category(int id, String name, String description) {
        this(id, "", name, description, true);
    }

    public Category(int id, String code, String name, String description, boolean available) {
        this.id = id;
        this.code = code == null ? "" : code;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
