package com.coffeeshop.client.model;

public final class PointTransaction {
    public final int id;
    public final String title;
    public final int points;
    public final String date;

    public PointTransaction(int id, String title, int points, String date) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.date = date;
    }
}
