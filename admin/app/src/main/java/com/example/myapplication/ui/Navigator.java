package com.example.myapplication.ui;

import com.example.myapplication.model.Category;

public interface Navigator {
    void showLogin();
    void showDashboard();
    void showRevenue();
    void showCategories();
    void showProducts(Category category);
    void logout();
}
