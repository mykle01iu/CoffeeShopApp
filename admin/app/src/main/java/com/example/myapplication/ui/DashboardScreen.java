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
import com.example.myapplication.data.BackendApi;
import com.example.myapplication.data.MockRevenue;
import com.example.myapplication.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class DashboardScreen {

    private static final String[] LOW_STOCK_ITEMS = {
            "Sữa đặc — còn 2 hộp",
            "Hạt cà phê Arabica — còn 800g"
    };

    private final Activity activity;
    private final Navigator navigator;
    private final AccountStore accounts;
    private final BackendApi backendApi;
    public DashboardScreen(Activity activity, Navigator navigator,
                           AccountStore accounts, BackendApi backendApi) {
        this.activity = activity;
        this.navigator = navigator;
        this.accounts = accounts;
        this.backendApi = backendApi;
    }

    public void show() {
        activity.setContentView(R.layout.dashboard);

        TextView date = activity.findViewById(R.id.dashboard_date);
        date.setText(new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));
        TextView greeting = activity.findViewById(R.id.dashboard_greeting);
        greeting.setText(accounts.isAdmin() ? "Chúc bạn một ngày làm việc hiệu quả." : "Chúc ca làm việc thuận lợi.");
        showOrdersLoading();
        loadOrders();
        populateRevenueSummary();
        populateLowStockAlerts();

        View approvalsButton = activity.findViewById(R.id.open_account_approvals);
        approvalsButton.setVisibility(accounts.isAdmin() ? View.VISIBLE : View.GONE);
        approvalsButton.setOnClickListener(v -> new AccountApprovalDialog(activity, accounts).show());

        View usersButton = activity.findViewById(R.id.open_users);
        usersButton.setVisibility(accounts.isAdmin() ? View.VISIBLE : View.GONE);
        usersButton.setOnClickListener(v -> navigator.showUsers());

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

    private void loadOrders() {
        backendApi.getAdminOrders(new BackendApi.Callback<List<Order>>() {
            @Override public void onSuccess(List<Order> result) {
                if (activity.findViewById(R.id.dashboard_screen) == null) return;
                List<Order> orders = result == null ? new ArrayList<>() : result;
                populateOrderSummary(orders);
                populateRecentOrders(orders);
            }
            @Override public void onError(BackendApi.ApiError error) {
                if (activity.findViewById(R.id.dashboard_screen) == null) return;
                setOrderCounts("—", "—", "—", "—");
                showRecentOrdersMessage("Không tải được đơn hàng: " + error.message);
            }
        });
    }

    private void showOrdersLoading() {
        setOrderCounts("…", "…", "…", "…");
        showRecentOrdersMessage("Đang tải đơn hàng từ backend...");
    }

    private void setOrderCounts(String waiting, String preparing, String completed, String pending) {
        ((TextView) activity.findViewById(R.id.orders_waiting_count)).setText(waiting);
        ((TextView) activity.findViewById(R.id.orders_preparing_count)).setText(preparing);
        ((TextView) activity.findViewById(R.id.orders_completed_count)).setText(completed);
        ((TextView) activity.findViewById(R.id.pending_orders_count)).setText(pending);
    }

    private void populateOrderSummary(List<Order> orders) {
        int waiting = 0, preparing = 0, completed = 0;
        for (Order order : orders) {
            if ("Chờ xác nhận".equals(order.status)) waiting++;
            else if ("Đang chuẩn bị".equals(order.status)) preparing++;
            else if ("Hoàn thành".equals(order.status)) completed++;
        }
        setOrderCounts(String.valueOf(waiting), String.valueOf(preparing),
                String.valueOf(completed), String.valueOf(waiting + preparing));
    }

    private void showRecentOrdersMessage(String message) {
        LinearLayout list = activity.findViewById(R.id.recent_orders_list);
        list.removeAllViews();
        TextView status = new TextView(activity);
        status.setText(message);
        status.setTextColor(Color.rgb(136, 125, 117));
        status.setTextSize(12);
        status.setPadding(0, UiUtils.dp(activity, 8), 0, UiUtils.dp(activity, 8));
        list.addView(status, new LinearLayout.LayoutParams(-1, -2));
    }

    private void populateRecentOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            showRecentOrdersMessage("Chưa có đơn hàng trên backend.");
            return;
        }
        LinearLayout list = activity.findViewById(R.id.recent_orders_list);
        list.removeAllViews();
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
            meta.setText(order.customer + " · " + order.dateTime.replace('T', ' '));
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
        if ("Đang chuẩn bị".equals(status)) return Color.rgb(92, 58, 150);
        if ("Hoàn thành".equals(status)) return Color.rgb(39, 112, 65);
        return Color.rgb(160, 54, 48);
    }

    private int statusBackground(String status) {
        if ("Chờ xác nhận".equals(status)) return Color.rgb(255, 243, 205);
        if ("Đang chuẩn bị".equals(status)) return Color.rgb(232, 222, 255);
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
