package com.example.myapplication.model;

public class Product {
    public static final String DEFAULT_IMAGE_URI = "android.resource://com.example.myapplication/drawable/splash";
    public final int id;
    public final int categoryId;
    public String name;
    public double price;
    public String description;
    public String imageUri;
    public Product(int id, int categoryId, String name, double price, String description, String imageUri) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUri = imageUri;
    }
}
