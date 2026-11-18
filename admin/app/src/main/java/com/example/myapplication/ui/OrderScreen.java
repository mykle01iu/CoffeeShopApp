package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.data.MockOrders;
import com.example.myapplication.model.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderScreen {
    private static final List<String> FILTERS = Arrays.asList("Tất cả", "Chờ xác nhận", "Đang pha chế", "Đang giao", "Hoàn thành", "Đã hủy");
    private final Activity activity;
    private final Navigator navigator;
    private final List<Order> orders = MockOrders.all();
    private LinearLayout filters, orderList;
    private String selected = "Tất cả";

    public OrderScreen(Activity activity, Navigator navigator) {
        this.activity = activity;
        this.navigator = navigator;
    }

    public void show() {
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(OrderUi.BG);
        LinearLayout root = OrderUi.column(activity);
        root.setPadding(OrderUi.dp(activity, 20), OrderUi.dp(activity, 18), OrderUi.dp(activity, 20), OrderUi.dp(activity, 26));
        scroll.addView(root);

        LinearLayout header = OrderUi.row(activity);
        TextView back = OrderUi.text(activity, "‹", 32, OrderUi.INK, false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(OrderUi.shape(Color.WHITE, OrderUi.dp(activity, 14)));
        header.addView(back, OrderUi.params(OrderUi.dp(activity, 44), OrderUi.dp(activity, 44)));
        back.setOnClickListener(v -> navigator.showDashboard());
        LinearLayout titleColumn = OrderUi.column(activity);
        TextView eyebrow = OrderUi.text(activity, "MỘC MIÊN  /  QUẢN TRỊ", 10, OrderUi.COFFEE, true);
        TextView title = OrderUi.text(activity, "Đơn hàng", 25, OrderUi.INK, true);
        titleColumn.addView(eyebrow);
        titleColumn.addView(title);
        LinearLayout.LayoutParams titleParams = OrderUi.params(0, -2);
        titleParams.weight = 1;
        titleParams.leftMargin = OrderUi.dp(activity, 12);
        header.addView(titleColumn, titleParams);
        root.addView(header);

        TextView subtitle = OrderUi.text(activity, orders.size() + " đơn hàng mẫu · Cập nhật hôm nay", 13, OrderUi.MUTED, false);
        LinearLayout.LayoutParams subParams = OrderUi.params(-2, -2);
        subParams.topMargin = OrderUi.dp(activity, 8);
        root.addView(subtitle, subParams);

        HorizontalScrollView horizontal = new HorizontalScrollView(activity);
        horizontal.setHorizontalScrollBarEnabled(false);
        filters = OrderUi.row(activity);
        LinearLayout.LayoutParams filterParams = OrderUi.params(-2, -2);
        filterParams.topMargin = OrderUi.dp(activity, 18);
        horizontal.addView(filters, filterParams);
        root.addView(horizontal);
        orderList = OrderUi.column(activity);
        LinearLayout.LayoutParams listParams = OrderUi.params(-1, -2);
        listParams.topMargin = OrderUi.dp(activity, 14);
        root.addView(orderList, listParams);
        activity.setContentView(scroll);
        renderFilters();
        renderOrders();
    }

    private void renderFilters() {
        filters.removeAllViews();
        for (String filter : FILTERS) {
            TextView chip = OrderUi.text(activity, filter, 12, filter.equals(selected) ? Color.WHITE : OrderUi.COFFEE, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(OrderUi.dp(activity, 14), OrderUi.dp(activity, 10), OrderUi.dp(activity, 14), OrderUi.dp(activity, 10));
            chip.setBackground(OrderUi.shape(filter.equals(selected) ? OrderUi.COFFEE : Color.WHITE, OrderUi.dp(activity, 20)));
            LinearLayout.LayoutParams p = OrderUi.params(-2, -2);
            p.rightMargin = OrderUi.dp(activity, 8);
            filters.addView(chip, p);
            chip.setOnClickListener(v -> { selected = filter; renderFilters(); renderOrders(); });
        }
    }

    private void renderOrders() {
        orderList.removeAllViews();
        List<Order> visible = new ArrayList<>();
        for (Order order : orders) if (selected.equals("Tất cả") || order.status.equals(selected)) visible.add(order);
        for (Order order : visible) {
            LinearLayout card = OrderUi.card(activity);
            LinearLayout top = OrderUi.row(activity);
            TextView id = OrderUi.text(activity, order.id, 14, OrderUi.INK, true);
            LinearLayout.LayoutParams idParams = OrderUi.params(0, -2);
            idParams.weight = 1;
            top.addView(id, idParams);
            top.addView(OrderUi.badge(activity, order.status));
            card.addView(top);
            TextView customer = OrderUi.text(activity, order.customer, 13, OrderUi.MUTED, false);
            LinearLayout.LayoutParams customerParams = OrderUi.params(-2, -2);
            customerParams.topMargin = OrderUi.dp(activity, 10);
            card.addView(customer, customerParams);
            TextView summary = OrderUi.text(activity, order.getItemSummary(), 13, OrderUi.INK, false);
            LinearLayout.LayoutParams summaryParams = OrderUi.params(-1, -2);
            summaryParams.topMargin = OrderUi.dp(activity, 5);
            card.addView(summary, summaryParams);
            LinearLayout bottom = OrderUi.row(activity);
            LinearLayout.LayoutParams bottomParams = OrderUi.params(-1, -2);
            bottomParams.topMargin = OrderUi.dp(activity, 13);
            TextView date = OrderUi.text(activity, order.dateTime, 11, OrderUi.MUTED, false);
            LinearLayout.LayoutParams dateParams = OrderUi.params(0, -2);
            dateParams.weight = 1;
            bottom.addView(date, dateParams);
            bottom.addView(OrderUi.text(activity, OrderUi.money(order.getTotal()), 14, OrderUi.COFFEE, true));
            card.addView(bottom);
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(v -> navigator.showOrderDetail(order));
            LinearLayout.LayoutParams p = OrderUi.params(-1, -2);
            p.bottomMargin = OrderUi.dp(activity, 10);
            orderList.addView(card, p);
        }
        if (visible.isEmpty()) {
            TextView empty = OrderUi.text(activity, "Không có đơn hàng ở trạng thái này.", 14, OrderUi.MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, OrderUi.dp(activity, 36), 0, OrderUi.dp(activity, 36));
            orderList.addView(empty);
        }
    }
}
