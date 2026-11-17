package com.example.myapplication.model;

public class Product {
    public static final String DEFAULT_IMAGE_URI = "android.resource://com.example.myapplication/drawable/splash";
    public final int id;
    public final int categoryId;
    public final String categoryCode;
    public String name;
    public double price;
    public String description;
    public String imageUri;
    public boolean available;

    public Product(int id, int categoryId, String name, double price, String description, String imageUri) {
        this(id, categoryId, "", name, price, description, imageUri, true);
    }

    public Product(int id, String categoryCode, String name, double price, String description,
                   String imageUrl, boolean available) {
        this(id, -1, categoryCode, name, price, description, imageUrl, available);
    }

    private Product(int id, int categoryId, String categoryCode, String name, double price,
                    String description, String imageUri, boolean available) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryCode = categoryCode == null ? "" : categoryCode;
        this.name = name;
        this.price = price;
        this.description = description == null ? "" : description;
        this.imageUri = imageUri;
        this.available = available;
    }
}
