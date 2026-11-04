package com.example.myapplication.ui;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.data.ProductRepository;

import java.text.NumberFormat;
import java.util.Locale;

public final class UiUtils {

    private UiUtils() {
    }

    public static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }

    public static String rawTextOf(EditText editText) {
        return editText.getText().toString();
    }

    public static void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(android.view.View.VISIBLE);
    }

    public static String formatPrice(int price) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price) + " ₫";
    }

    public static String categoryIcon(String category) {
        switch (category) {
            case ProductRepository.CAT_COFFEE: return "☕";
            case ProductRepository.CAT_TEA: return "🍵";
            default: return "🥐";
        }
    }
}