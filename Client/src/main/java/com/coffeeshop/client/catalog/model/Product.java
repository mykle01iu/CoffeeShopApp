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
    public final String imageUrl;

    public Product(int id, String name, String category, int price, float rating,
                   int reviewCount, String description, int imageRes) {
        this(id, name, category, price, rating, reviewCount, description, imageRes, null);
    }

    public Product(int id, String name, String category, int price, float rating,
                   int reviewCount, String description, int imageRes, String imageUrl) {
        this.id = id; this.name = name; this.category = category; this.price = price;
        this.rating = rating; this.reviewCount = reviewCount;
        this.description = description; this.imageRes = imageRes; this.imageUrl = imageUrl;
    }
}
