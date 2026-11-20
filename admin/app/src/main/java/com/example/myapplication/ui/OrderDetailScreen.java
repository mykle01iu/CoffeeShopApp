package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.model.Order;

public class OrderDetailScreen {
    private final Activity activity;
    private final Navigator navigator;
    private final Order order;

    public OrderDetailScreen(Activity activity, Navigator navigator, Order order) {
        this.activity = activity;
        this.navigator = navigator;
        this.order = order;
    }

    public void show() {
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(OrderUi.BG);
        LinearLayout root = OrderUi.column(activity);
        root.setPadding(OrderUi.dp(activity, 20), OrderUi.dp(activity, 18), OrderUi.dp(activity, 20), OrderUi.dp(activity, 28));
        scroll.addView(root);

        LinearLayout header = OrderUi.row(activity);
        TextView back = OrderUi.text(activity, "‹", 32, OrderUi.INK, false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(OrderUi.shape(Color.WHITE, OrderUi.dp(activity, 14)));
        header.addView(back, OrderUi.params(OrderUi.dp(activity, 44), OrderUi.dp(activity, 44)));
        back.setOnClickListener(v -> navigator.showOrders());
        LinearLayout heading = OrderUi.column(activity);
        heading.addView(OrderUi.text(activity, "CHI TIẾT ĐƠN", 10, OrderUi.COFFEE, true));
        heading.addView(OrderUi.text(activity, order.id, 19, OrderUi.INK, true));
        LinearLayout.LayoutParams headingParams = OrderUi.params(0, -2);
        headingParams.weight = 1;
        headingParams.leftMargin = OrderUi.dp(activity, 12);
        header.addView(heading, headingParams);
        root.addView(header);

        LinearLayout statusCard = OrderUi.card(activity);
        LinearLayout.LayoutParams statusParams = OrderUi.params(-1, -2);
        statusParams.topMargin = OrderUi.dp(activity, 18);
        LinearLayout statusRow = OrderUi.row(activity);
        LinearLayout statusInfo = OrderUi.column(activity);
        statusInfo.addView(OrderUi.text(activity, "Trạng thái đơn hàng", 12, OrderUi.MUTED, false));
        TextView date = OrderUi.text(activity, order.dateTime, 13, OrderUi.INK, true);
        LinearLayout.LayoutParams dateParams = OrderUi.params(-2, -2);
        dateParams.topMargin = OrderUi.dp(activity, 5);
        statusInfo.addView(date, dateParams);
        LinearLayout.LayoutParams infoParams = OrderUi.params(0, -2);
        infoParams.weight = 1;
        statusRow.addView(statusInfo, infoParams);
        statusRow.addView(OrderUi.badge(activity, order.status));
        statusCard.addView(statusRow);
        root.addView(statusCard, statusParams);

        LinearLayout customer = section("Khách hàng");
        addInfo(customer, "Tên khách", order.customer);
        addInfo(customer, "Số điện thoại", order.phone);
        addInfo(customer, order.fulfillment, order.address);
        addInfo(customer, "Ghi chú", order.note.isEmpty() ? "Không có" : order.note);
        addSection(root, customer);

        LinearLayout products = section("Sản phẩm trong đơn");
        for (Order.Item item : order.items) {
            LinearLayout line = OrderUi.row(activity);
            LinearLayout productInfo = OrderUi.column(activity);
            productInfo.addView(OrderUi.text(activity, item.name, 13, OrderUi.INK, true));
            TextView quantity = OrderUi.text(activity, item.quantity + " × " + OrderUi.money(item.unitPrice), 12, OrderUi.MUTED, false);
            LinearLayout.LayoutParams q = OrderUi.params(-2, -2);
            q.topMargin = OrderUi.dp(activity, 4);
            productInfo.addView(quantity, q);
            LinearLayout.LayoutParams productParams = OrderUi.params(0, -2);
            productParams.weight = 1;
            line.addView(productInfo, productParams);
            line.addView(OrderUi.text(activity, OrderUi.money(item.quantity * item.unitPrice), 13, OrderUi.INK, true));
            products.addView(line);
            ViewDivider.add(products, activity);
        }
        addSection(root, products);

        LinearLayout payment = section("Thanh toán");
        addInfo(payment, "Phương thức", order.payment);
        addInfo(payment, "Tạm tính", OrderUi.money(order.getSubtotal()));
        addInfo(payment, "Phí giao hàng", order.deliveryFee == 0 ? "Miễn phí" : OrderUi.money(order.deliveryFee));
        LinearLayout totalRow = OrderUi.row(activity);
        TextView totalLabel = OrderUi.text(activity, "Tổng cộng", 14, OrderUi.INK, true);
        LinearLayout.LayoutParams totalLabelParams = OrderUi.params(0, -2);
        totalLabelParams.weight = 1;
        totalRow.addView(totalLabel, totalLabelParams);
        totalRow.addView(OrderUi.text(activity, OrderUi.money(order.getTotal()), 17, OrderUi.COFFEE, true));
        LinearLayout.LayoutParams totalParams = OrderUi.params(-1, -2);
        totalParams.topMargin = OrderUi.dp(activity, 12);
        payment.addView(totalRow, totalParams);
        addSection(root, payment);

        activity.setContentView(scroll);
    }

    private LinearLayout section(String title) {
        LinearLayout card = OrderUi.card(activity);
        TextView heading = OrderUi.text(activity, title, 16, OrderUi.INK, true);
        card.addView(heading);
        return card;
    }

    private void addSection(LinearLayout root, LinearLayout section) {
        LinearLayout.LayoutParams p = OrderUi.params(-1, -2);
        p.topMargin = OrderUi.dp(activity, 12);
        root.addView(section, p);
    }

    private void addInfo(LinearLayout parent, String label, String value) {
        LinearLayout row = OrderUi.row(activity);
        TextView left = OrderUi.text(activity, label, 12, OrderUi.MUTED, false);
        LinearLayout.LayoutParams lp = OrderUi.params(OrderUi.dp(activity, 112), -2);
        row.addView(left, lp);
        TextView right = OrderUi.text(activity, value, 12, OrderUi.INK, true);
        right.setGravity(Gravity.END);
        LinearLayout.LayoutParams rightParams = OrderUi.params(0, -2);
        rightParams.weight = 1;
        row.addView(right, rightParams);
        LinearLayout.LayoutParams rowParams = OrderUi.params(-1, -2);
        rowParams.topMargin = OrderUi.dp(activity, 12);
        parent.addView(row, rowParams);
    }

    private static final class ViewDivider {
        static void add(LinearLayout parent, Activity activity) {
            android.view.View divider = new android.view.View(activity);
            divider.setBackgroundColor(Color.rgb(238, 232, 225));
            LinearLayout.LayoutParams p = OrderUi.params(-1, OrderUi.dp(activity, 1));
            p.topMargin = OrderUi.dp(activity, 11);
            p.bottomMargin = OrderUi.dp(activity, 11);
            parent.addView(divider, p);
        }
    }
}
