package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.myapplication.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ProductImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ProductImageLoader() { }

    static void load(ImageView view, String value) {
        view.setImageResource(R.drawable.splash);
        if (value == null || value.trim().isEmpty()) {
            view.setTag(null);
            return;
        }

        String imageUrl = value.trim();
        view.setTag(imageUrl);
        Uri uri = Uri.parse(imageUrl);
        String scheme = uri.getScheme();
        if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            EXECUTOR.execute(() -> loadRemote(view, imageUrl));
            return;
        }
        try {
            view.setImageURI(uri);
        } catch (RuntimeException ignored) {
            view.setImageResource(R.drawable.splash);
        }
    }

    private static void loadRemote(ImageView view, String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "image/*");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return;
            Bitmap bitmap;
            try (InputStream input = connection.getInputStream()) {
                bitmap = BitmapFactory.decodeStream(input);
            }
            if (bitmap != null) {
                MAIN_HANDLER.post(() -> {
                    if (imageUrl.equals(view.getTag())) view.setImageBitmap(bitmap);
                });
            }
        } catch (Exception ignored) {
            // Keep the placeholder when the image URL is unavailable or invalid.
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
