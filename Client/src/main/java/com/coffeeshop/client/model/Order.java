package com.coffeeshop.client.model;

import java.util.Collections;
import java.util.List;

public final class Order {
    public final String id;
    public final List<CartItem> items;
    public final int subtotal;
    public final int shippingFee;
    public final int discount;
    public final int total;
    public final String voucherCode;
    public final PaymentMethod paymentMethod;
    public final String address;
    public final String receiverName;
    public final String phone;
    public final String note;
    public final String createdAt;
    public final OrderStatus status;
    public final int pointsEarned;

    public Order(String id, List<CartItem> items, int subtotal, int shippingFee, int discount,
                 int total, String voucherCode, PaymentMethod paymentMethod, String address,
                 String note, String createdAt, OrderStatus status, int pointsEarned) {
        this.id = id;
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.total = total;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.receiverName = "";
        this.phone = "";
        this.note = note;
        this.createdAt = createdAt;
        this.status = status;
        this.pointsEarned = pointsEarned;
    }

    public Order(String id, List<CartItem> items, int subtotal, int shippingFee, int discount,
                 int total, String voucherCode, PaymentMethod paymentMethod, String receiverName,
                 String phone, String address, String note, String createdAt, OrderStatus status, int pointsEarned) {
        this.id=id; this.items=items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.subtotal=subtotal; this.shippingFee=shippingFee; this.discount=discount; this.total=total;
        this.voucherCode=voucherCode; this.paymentMethod=paymentMethod; this.receiverName=receiverName;
        this.phone=phone; this.address=address; this.note=note; this.createdAt=createdAt; this.status=status; this.pointsEarned=pointsEarned;
    }
}
