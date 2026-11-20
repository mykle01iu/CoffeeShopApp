package com.coffeeshop.client.data.payment;

public class PaymentMethod {
    private final String id;
    private final PaymentType type;
    private final String displayName;
    private final String detail;
    private final boolean isDefault;
    private final String provider;
    private final String maskedPhone;
    private final String cardBrand;
    private final String cardNumberLast4;
    private final String expiryDate;

    public PaymentMethod(String id, PaymentType type, String displayName, String detail, boolean isDefault,
                         String provider, String maskedPhone, String cardBrand, String cardNumberLast4, String expiryDate) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.detail = detail;
        this.isDefault = isDefault;
        this.provider = provider;
        this.maskedPhone = maskedPhone;
        this.cardBrand = cardBrand;
        this.cardNumberLast4 = cardNumberLast4;
        this.expiryDate = expiryDate;
    }

    public static PaymentMethod createCod() {
        return new PaymentMethod(
                "cod_default",
                PaymentType.COD,
                "Tiền mặt khi nhận hàng",
                "Thanh toán trực tiếp cho shipper khi nhận món",
                true,
                null, null, null, null, null
        );
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

    public String getProvider() {
        return provider;
    }

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getCardNumberLast4() {
        return cardNumberLast4;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getSnapshotString() {
        if (type == PaymentType.COD) {
            return "Tiền mặt khi nhận hàng (COD)";
        }
        return displayName + " (" + detail + ")";
    }
}
