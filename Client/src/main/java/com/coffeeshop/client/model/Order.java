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
    public final String paymentDisplayName;
    public final String paymentDetail;
    public final String address;
    public final String receiverName;
    public final String phone;
    public final String note;
    public final String createdAt;
    public final OrderStatus status;
    public final int pointsEarned;
    public final boolean remoteOrder;

    public Order(String id, List<CartItem> items, int subtotal, int shippingFee, int discount,
                 int total, String voucherCode, PaymentMethod paymentMethod, String paymentDisplayName,
                 String paymentDetail, String receiverName, String phone, String address,
                 String note, String createdAt, OrderStatus status, int pointsEarned) {
        this(id, items, subtotal, shippingFee, discount, total, voucherCode, paymentMethod,
                paymentDisplayName, paymentDetail, receiverName, phone, address, note,
                createdAt, status, pointsEarned, false);
    }

    public Order(String id, List<CartItem> items, int subtotal, int shippingFee, int discount,
                 int total, String voucherCode, PaymentMethod paymentMethod, String paymentDisplayName,
                 String paymentDetail, String receiverName, String phone, String address,
                 String note, String createdAt, OrderStatus status, int pointsEarned, boolean remoteOrder) {
        this.id = id;
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.total = total;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
        this.paymentDisplayName = paymentDisplayName != null ? paymentDisplayName : (paymentMethod != null ? paymentMethod.name() : "Tiền mặt");
        this.paymentDetail = paymentDetail != null ? paymentDetail : "";
        this.receiverName = receiverName;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.createdAt = createdAt;
        this.status = status;
        this.pointsEarned = pointsEarned;
        this.remoteOrder = remoteOrder;
    }

    public Order(String id, List<CartItem> items, int subtotal, int shippingFee, int discount,
                 int total, String voucherCode, PaymentMethod paymentMethod, String receiverName,
                 String phone, String address, String note, String createdAt, OrderStatus status, int pointsEarned) {
        this(id, items, subtotal, shippingFee, discount, total, voucherCode, paymentMethod,
                paymentMethod != null ? paymentMethod.name() : "Tiền mặt", "", receiverName, phone, address, note, createdAt, status, pointsEarned);
    }

    public String getPaymentSnapshotText() {
        if (paymentDisplayName == null || paymentDisplayName.isEmpty()) {
            return "Tiền mặt khi nhận hàng (COD)";
        }
        if (paymentDetail != null && !paymentDetail.isEmpty()) {
            return paymentDisplayName + " (" + paymentDetail + ")";
        }
        return paymentDisplayName;
    }
}
