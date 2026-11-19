package com.coffeeshop.client.data.legal;

public enum LegalType {
    TERMS("Điều khoản sử dụng"),
    PRIVACY("Chính sách bảo mật");

    private final String title;

    LegalType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
