package com.example.myapplication.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.model.Order;

import java.text.NumberFormat;
import java.util.Locale;

final class OrderUi {
    static final int BG = Color.rgb(248, 246, 241);
    static final int INK = Color.rgb(46, 36, 31);
    static final int MUTED = Color.rgb(136, 125, 117);
    static final int COFFEE = Color.rgb(112, 69, 47);
    private OrderUi() {}

    static int dp(Context c, int value) { return UiUtils.dp(c, value); }

    static LinearLayout column(Context c) {
        LinearLayout view = new LinearLayout(c);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    static LinearLayout row(Context c) {
        LinearLayout view = new LinearLayout(c);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    static TextView text(Context c, String value, int size, int color, boolean bold) {
        TextView view = new TextView(c);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    static GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dpColorRadius(radius));
        return d;
    }

    private static int dpColorRadius(int radius) { return radius; }

    static TextView badge(Context c, String status) {
        int bg, fg;
        switch (status) {
            case "Chờ xác nhận": bg = Color.rgb(255, 243, 205); fg = Color.rgb(143, 94, 0); break;
            case "Đang pha chế": bg = Color.rgb(232, 222, 255); fg = Color.rgb(92, 58, 150); break;
            case "Đang giao": bg = Color.rgb(222, 237, 255); fg = Color.rgb(37, 91, 145); break;
            case "Hoàn thành": bg = Color.rgb(220, 244, 228); fg = Color.rgb(39, 112, 65); break;
            default: bg = Color.rgb(252, 226, 224); fg = Color.rgb(160, 54, 48); break;
        }
        TextView badge = text(c, status, 11, fg, true);
        badge.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(bg);
        bgShape.setCornerRadius(dp(c, 20));
        badge.setBackground(bgShape);
        return badge;
    }

    static LinearLayout card(Context c) {
        LinearLayout card = column(c);
        card.setPadding(dp(c, 16), dp(c, 15), dp(c, 16), dp(c, 15));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(c, 18));
        bg.setStroke(dp(c, 1), Color.rgb(240, 232, 225));
        card.setBackground(bg);
        return card;
    }

    static String money(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    static LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    static void addSpace(LinearLayout parent, Context c, int height) {
        View spacer = new View(c);
        parent.addView(spacer, params(1, dp(c, height)));
    }
}
