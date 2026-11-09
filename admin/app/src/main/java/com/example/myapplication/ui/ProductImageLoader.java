package com.example.myapplication.ui;

import android.net.Uri;
import android.widget.ImageView;

import com.example.myapplication.R;

final class ProductImageLoader {
    private ProductImageLoader() { }

    static void load(ImageView view, String value) {
        view.setImageResource(R.drawable.splash);
        if (value == null || value.trim().isEmpty()) return;
        String imageUrl = value.trim();
        view.setTag(imageUrl);
        try { view.setImageURI(Uri.parse(imageUrl)); } catch (RuntimeException ignored) { }
    }
}
