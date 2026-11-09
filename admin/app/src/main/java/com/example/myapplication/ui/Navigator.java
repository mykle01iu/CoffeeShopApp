package com.example.myapplication.ui;

import android.net.Uri;

import com.example.myapplication.model.Category;

public interface Navigator {
    void showLogin();
    void showDashboard();
    void showRevenue();
    void showCategories();
    void showProducts(Category category);
    void pickProductImage(ProductImageCallback callback);
    void logout();

    interface ProductImageCallback {
        void onImagePicked(Uri uri);
    }
}
