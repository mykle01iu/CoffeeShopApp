package com.coffeeshop.client.data.payment;

public class PaymentMethod {
    private final String id;
    private final PaymentType type;
    private final String displayName;
    private final String detail;
    private final boolean isDefault;

    public PaymentMethod(String id, PaymentType type, String displayName, String detail, boolean isDefault) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.detail = detail;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public PaymentType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean canDelete() {
        return type != PaymentType.COD;
    }
}
