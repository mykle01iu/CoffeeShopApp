package com.coffeeshop.client.data.support;

public enum TicketTopic {
    ORDER("Đơn hàng"),
    PAYMENT("Thanh toán"),
    VOUCHER("Voucher"),
    FEEDBACK("Góp ý"),
    OTHER("Khác");

    private final String label;

    TicketTopic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
