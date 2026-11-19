package com.example.myapplication.ui;

import com.example.myapplication.model.Category;
import com.example.myapplication.model.Order;

public interface Navigator {
    void showLogin();
    void showDashboard();
    void showRevenue();
    void showCategories();
    void showOrders();
    void showOrderDetail(Order order);
    void showProducts(Category category);
    void logout();
}
