package com.coffeeshop.client.data.payment;

public enum PaymentType {
    COD("Tiền mặt"),
    WALLET("Ví điện tử"),
    CARD("Thẻ ngân hàng");

    private final String label;

    PaymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
