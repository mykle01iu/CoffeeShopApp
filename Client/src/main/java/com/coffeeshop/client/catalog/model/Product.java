package com.coffeeshop.client.catalog.model;

public class Product {
    public final int id;
    public final String name;
    public final String category;
    public final int price;
    public final float rating;
    public final int reviewCount;
    public final String description;
    public final int imageRes;

    public Product(int id, String name, String category, int price, float rating,
                   int reviewCount, String description, int imageRes) {
        this.id = id; this.name = name; this.category = category; this.price = price;
        this.rating = rating; this.reviewCount = reviewCount;
        this.description = description; this.imageRes = imageRes;
    }
}
