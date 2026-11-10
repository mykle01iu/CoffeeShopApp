package com.coffeeshop.client.model;

public final class Voucher {
    public final String code;
    public final String title;
    public final String condition;
    public final String note;
    public final int iconRes;
    public final VoucherStatus status;
    public final VoucherType type;
    public final int value;
    public final int minOrder;
    public final Integer maxDiscount;
    public final boolean firstOrderOnly;
    public final String usedDate;

    public Voucher(String code, String title, String condition, String note, int iconRes, VoucherStatus status,
                   VoucherType type, int value, int minOrder, Integer maxDiscount, boolean firstOrderOnly, String usedDate) {
        this.code = code;
        this.title = title;
        this.condition = condition;
        this.note = note;
        this.iconRes = iconRes;
        this.status = status;
        this.type = type;
        this.value = value;
        this.minOrder = minOrder;
        this.maxDiscount = maxDiscount;
        this.firstOrderOnly = firstOrderOnly;
        this.usedDate = usedDate;
    }
}
