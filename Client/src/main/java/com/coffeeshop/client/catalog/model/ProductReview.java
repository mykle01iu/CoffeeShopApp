package com.coffeeshop.client.catalog.model;

public final class ProductReview {
    public final String author;
    public final float rating;
    public final String comment;
    public final String date;

    public ProductReview(String author, float rating, String comment, String date) {
        this.author=author; this.rating=rating; this.comment=comment; this.date=date;
    }
}
