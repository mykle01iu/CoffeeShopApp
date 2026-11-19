package com.coffeeshop.client.ui.home;

public final class DrinkProduct {
    public final String id;
    public final String name;
    public final String description;
    public final String category;
    public final int priceVnd;
    public final float rating;
    public final int imageRes;

    public DrinkProduct(String id, String name, String description, String category,
                        int priceVnd, float rating, int imageRes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceVnd = priceVnd;
        this.rating = rating;
        this.imageRes = imageRes;
    }
}
