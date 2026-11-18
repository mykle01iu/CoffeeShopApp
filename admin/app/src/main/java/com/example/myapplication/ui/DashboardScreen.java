package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.example.myapplication.auth.AccountStore;
import com.example.myapplication.data.MockOrders;
import com.example.myapplication.data.MockRevenue;
import com.example.myapplication.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardScreen {

    private static final String[] LOW_STOCK_ITEMS = {
            "Sữa đặc — còn 2 hộp",
            "Hạt cà phê Arabica — còn 800g"
    };

    private final Activity activity;
    private final Navigator navigator;
    private final AccountStore accounts;
    public DashboardScreen(Activity activity, Navigator navigator,
                           AccountStore accounts) {
        this.activity = activity;
        this.navigator = navigator;
        this.accounts = accounts;
    }

    public void show() {
        activity.setContentView(R.layout.dashboard);

        TextView date = activity.findViewById(R.id.dashboard_date);
        date.setText(new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));
        TextView greeting = activity.findViewById(R.id.dashboard_greeting);
        greeting.setText(accounts.isAdmin() ? "Chúc bạn một ngày làm việc hiệu quả." : "Chúc ca làm việc thuận lợi.");
        populateOrderSummary();
        populateRecentOrders();
        populateRevenueSummary();
        populateLowStockAlerts();

        View approvalsButton = activity.findViewById(R.id.open_account_approvals);
        approvalsButton.setVisibility(accounts.isAdmin() ? View.VISIBLE : View.GONE);
        approvalsButton.setOnClickListener(v -> new AccountApprovalDialog(activity, accounts).show());

        activity.findViewById(R.id.logout_button).setOnClickListener(v -> navigator.logout());
        activity.findViewById(R.id.open_orders).setOnClickListener(v -> navigator.showOrders());
        activity.findViewById(R.id.open_orders_recent).setOnClickListener(v -> navigator.showOrders());
        activity.findViewById(R.id.revenue_summary_card).setOnClickListener(v -> navigator.showRevenue());
        activity.findViewById(R.id.open_inventory).setOnClickListener(v -> showInventoryDetails());
        activity.findViewById(R.id.open_categories).setOnClickListener(v -> navigator.showCategories());
    }

    private void populateRevenueSummary() {
        Calendar today = Calendar.getInstance(new Locale("vi", "VN"));
        Calendar weekStart = MockRevenue.startOfWeek(today);
        Calendar weekEnd = MockRevenue.endOfWeek(today);
        if (weekEnd.after(today)) weekEnd = (Calendar) today.clone();
        Calendar monthStart = (Calendar) today.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        MockRevenue.atStartOfDay(monthStart);
        Calendar monthEnd = (Calendar) today.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (monthEnd.after(today)) monthEnd = (Calendar) today.clone();
        ((TextView) activity.findViewById(R.id.dashboard_revenue_today))
                .setText(OrderUi.money(MockRevenue.forDay(today)));
        ((TextView) activity.findViewById(R.id.dashboard_revenue_week))
                .setText(OrderUi.money(MockRevenue.forRange(weekStart, weekEnd)));
        ((TextView) activity.findViewById(R.id.dashboard_revenue_month))
                .setText(OrderUi.money(MockRevenue.forRange(monthStart, monthEnd)));
    }

    private void populateOrderSummary() {
        int waiting = 0, preparing = 0, delivery = 0;
        for (Order order : MockOrders.all()) {
            if ("Chờ xác nhận".equals(order.status)) waiting++;
            else if ("Đang pha chế".equals(order.status)) preparing++;
            else if ("Đang giao".equals(order.status)) delivery++;
        }
        ((TextView) activity.findViewById(R.id.orders_waiting_count)).setText(String.valueOf(waiting));
        ((TextView) activity.findViewById(R.id.orders_preparing_count)).setText(String.valueOf(preparing));
        ((TextView) activity.findViewById(R.id.orders_delivery_count)).setText(String.valueOf(delivery));
        ((TextView) activity.findViewById(R.id.pending_orders_count)).setText(String.valueOf(waiting + preparing));
    }

    private void populateRecentOrders() {
        LinearLayout list = activity.findViewById(R.id.recent_orders_list);
        list.removeAllViews();
        List<Order> orders = MockOrders.all();
        int shown = Math.min(3, orders.size());
        for (int i = 0; i < shown; i++) {
            Order order = orders.get(i);
            LinearLayout row = new LinearLayout(activity);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, UiUtils.dp(activity, 7), 0, UiUtils.dp(activity, 7));

            LinearLayout details = new LinearLayout(activity);
            details.setOrientation(LinearLayout.VERTICAL);
            TextView orderId = new TextView(activity);
            orderId.setText(order.id);
            orderId.setTextColor(Color.rgb(46, 36, 31));
            orderId.setTextSize(13);
            orderId.setTypeface(null, Typeface.BOLD);
            details.addView(orderId);
            TextView meta = new TextView(activity);
            meta.setText(order.customer + " · " + order.dateTime.substring(order.dateTime.indexOf('·') + 1).trim());
            meta.setTextColor(Color.rgb(136, 125, 117));
            meta.setTextSize(11);
            LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(-2, -2);
            metaParams.topMargin = UiUtils.dp(activity, 3);
            details.addView(meta, metaParams);
            LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(0, -2, 1);
            row.addView(details, detailsParams);

            TextView status = new TextView(activity);
            status.setText(order.status);
            status.setTextSize(10);
            status.setTypeface(null, Typeface.BOLD);
            status.setTextColor(statusColor(order.status));
            status.setPadding(UiUtils.dp(activity, 8), UiUtils.dp(activity, 6), UiUtils.dp(activity, 8), UiUtils.dp(activity, 6));
            GradientDrawable badge = new GradientDrawable();
            badge.setColor(statusBackground(order.status));
            badge.setCornerRadius(UiUtils.dp(activity, 20));
            status.setBackground(badge);
            row.addView(status);
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> navigator.showOrderDetail(order));
            list.addView(row, new LinearLayout.LayoutParams(-1, -2));

            if (i < shown - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(Color.rgb(238, 232, 225));
                list.addView(divider, new LinearLayout.LayoutParams(-1, UiUtils.dp(activity, 1)));
            }
        }
    }

    private int statusColor(String status) {
        if ("Chờ xác nhận".equals(status)) return Color.rgb(143, 94, 0);
        if ("Đang pha chế".equals(status)) return Color.rgb(92, 58, 150);
        if ("Đang giao".equals(status)) return Color.rgb(37, 91, 145);
        if ("Hoàn thành".equals(status)) return Color.rgb(39, 112, 65);
        return Color.rgb(160, 54, 48);
    }

    private int statusBackground(String status) {
        if ("Chờ xác nhận".equals(status)) return Color.rgb(255, 243, 205);
        if ("Đang pha chế".equals(status)) return Color.rgb(232, 222, 255);
        if ("Đang giao".equals(status)) return Color.rgb(222, 237, 255);
        if ("Hoàn thành".equals(status)) return Color.rgb(220, 244, 228);
        return Color.rgb(252, 226, 224);
    }

    private void populateLowStockAlerts() {
        LinearLayout alerts = activity.findViewById(R.id.low_stock_list);
        alerts.removeAllViews();
        for (int i = 0; i < LOW_STOCK_ITEMS.length; i++) {
            TextView item = new TextView(activity);
            item.setText(LOW_STOCK_ITEMS[i]);
            item.setTextColor(Color.rgb(180, 35, 24));
            item.setTextSize(13);
            item.setTypeface(null, android.graphics.Typeface.BOLD);
            if (i > 0) item.setPadding(0, UiUtils.dp(activity, 6), 0, 0);
            alerts.addView(item);
        }
    }

    private void showInventoryDetails() {
        StringBuilder details = new StringBuilder();
        for (String item : LOW_STOCK_ITEMS) {
            details.append("• ").append(item).append("\n");
        }
        new AlertDialog.Builder(activity)
                .setTitle("Kho · Cảnh báo tồn kho")
                .setMessage(details.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

}
