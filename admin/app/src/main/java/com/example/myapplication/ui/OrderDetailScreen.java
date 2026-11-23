package com.example.myapplication.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.BackendApi;
import com.example.myapplication.model.Order;

import java.util.List;

public class OrderDetailScreen {
    private final Activity activity;
    private final Navigator navigator;
    private final Order order;
    private final BackendApi backendApi;
    private LinearLayout historySection;

    public OrderDetailScreen(Activity activity, Navigator navigator, Order order, BackendApi backendApi) {
        this.activity = activity;
        this.navigator = navigator;
        this.order = order;
        this.backendApi = backendApi;
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
        if (hasNextStatus()) {
            TextView updateStatus = OrderUi.text(activity, "Cập nhật trạng thái", 13, Color.WHITE, true);
            updateStatus.setGravity(Gravity.CENTER);
            updateStatus.setPadding(0, OrderUi.dp(activity, 11), 0, OrderUi.dp(activity, 11));
            updateStatus.setBackground(OrderUi.shape(OrderUi.COFFEE, OrderUi.dp(activity, 12)));
            LinearLayout.LayoutParams actionParams = OrderUi.params(-1, -2);
            actionParams.topMargin = OrderUi.dp(activity, 14);
            statusCard.addView(updateStatus, actionParams);
            updateStatus.setOnClickListener(v -> showStatusChoices());
        }
        root.addView(statusCard, statusParams);

        historySection = section("Lịch sử trạng thái");
        TextView historyLoading = OrderUi.text(activity, "Đang tải lịch sử...", 12, OrderUi.MUTED, false);
        LinearLayout.LayoutParams historyLoadingParams = OrderUi.params(-1, -2);
        historyLoadingParams.topMargin = OrderUi.dp(activity, 12);
        historySection.addView(historyLoading, historyLoadingParams);
        addSection(root, historySection);
        loadHistory();

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

    private long orderId() {
        return Long.parseLong(order.id.replace("#", ""));
    }

    private boolean hasNextStatus() {
        return "Chờ xác nhận".equals(order.status) || "Đang chuẩn bị".equals(order.status);
    }

    private void showStatusChoices() {
        final String[] statuses;
        final String[] labels;
        if ("Chờ xác nhận".equals(order.status)) {
            statuses = new String[]{"PREPARING", "CANCELLED"};
            labels = new String[]{"Đang chuẩn bị", "Đã hủy"};
        } else {
            statuses = new String[]{"COMPLETED", "CANCELLED"};
            labels = new String[]{"Hoàn thành", "Đã hủy"};
        }
        new AlertDialog.Builder(activity)
                .setTitle("Chuyển trạng thái đơn")
                .setItems(labels, (dialog, which) -> updateStatus(statuses[which]))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateStatus(String status) {
        backendApi.updateOrderStatus(orderId(), status, new BackendApi.Callback<String>() {
            @Override public void onSuccess(String result) {
                Toast.makeText(activity, "Đã cập nhật trạng thái đơn hàng.", Toast.LENGTH_SHORT).show();
                navigator.showOrders();
            }
            @Override public void onError(BackendApi.ApiError error) {
                Toast.makeText(activity, "Không cập nhật được: " + error.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadHistory() {
        backendApi.getOrderHistory(orderId(), new BackendApi.Callback<List<String>>() {
            @Override public void onSuccess(List<String> history) {
                historySection.removeAllViews();
                if (history == null || history.isEmpty()) {
                    historySection.addView(OrderUi.text(activity, "Chưa có lịch sử trạng thái.", 12, OrderUi.MUTED, false));
                    return;
                }
                for (String entry : history) {
                    TextView row = OrderUi.text(activity, "•  " + entry, 12, OrderUi.INK, false);
                    LinearLayout.LayoutParams params = OrderUi.params(-1, -2);
                    params.topMargin = OrderUi.dp(activity, 10);
                    historySection.addView(row, params);
                }
            }
            @Override public void onError(BackendApi.ApiError error) {
                historySection.removeAllViews();
                historySection.addView(OrderUi.text(activity, "Không tải được lịch sử: " + error.message,
                        12, OrderUi.MUTED, false));
            }
        });
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
