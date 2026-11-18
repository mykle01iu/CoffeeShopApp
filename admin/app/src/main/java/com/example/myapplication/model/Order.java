package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

/** Immutable order fixture used by the Admin order screens. */
public class Order {
    public static class Item {
        public final String name;
        public final int quantity;
        public final int unitPrice;

        public Item(String name, int quantity, int unitPrice) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    public final String id;
    public final String customer;
    public final String phone;
    public final String dateTime;
    public final String status;
    public final String fulfillment;
    public final String address;
    public final String payment;
    public final String note;
    public final int deliveryFee;
    public final List<Item> items;

    public Order(String id, String customer, String phone, String dateTime, String status,
                 String fulfillment, String address, String payment, String note,
                 int deliveryFee, Item... items) {
        this.id = id;
        this.customer = customer;
        this.phone = phone;
        this.dateTime = dateTime;
        this.status = status;
        this.fulfillment = fulfillment;
        this.address = address;
        this.payment = payment;
        this.note = note;
        this.deliveryFee = deliveryFee;
        this.items = new ArrayList<>();
        java.util.Collections.addAll(this.items, items);
    }

    public int getSubtotal() {
        int total = 0;
        for (Item item : items) total += item.quantity * item.unitPrice;
        return total;
    }

    public int getTotal() { return getSubtotal() + deliveryFee; }

    public String getItemSummary() {
        int count = 0;
        for (Item item : items) count += item.quantity;
        return count + " món · " + items.get(0).name + (items.size() > 1 ? " và " + (items.size() - 1) + " món khác" : "");
    }
}
