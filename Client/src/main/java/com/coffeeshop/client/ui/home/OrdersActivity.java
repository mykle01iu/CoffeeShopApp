package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.ui.ProductListActivity;
import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.model.CartItem;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.model.OrderStatus;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends Activity {

    private OrderStatus selectedFilter = null; // null = Tất cả
    private TextView chipAll, chipPreparing, chipCompleted, chipCancelled;
    private RecyclerView recyclerView;
    private View emptyView;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.ordersTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.ordersBack).setOnClickListener(v -> finish());

        chipAll = findViewById(R.id.chipOrderAll);
        chipPreparing = findViewById(R.id.chipOrderPreparing);
        chipCompleted = findViewById(R.id.chipOrderCompleted);
        chipCancelled = findViewById(R.id.chipOrderCancelled);

        recyclerView = findViewById(R.id.ordersRecyclerView);
        emptyView = findViewById(R.id.ordersEmptyView);

        findViewById(R.id.btnOrderNow).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductListActivity.class));
            finish();
        });

        adapter = new OrderHistoryAdapter(order -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderId", order.id);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chipAll.setOnClickListener(v -> selectFilter(null));
        chipPreparing.setOnClickListener(v -> selectFilter(OrderStatus.PREPARING));
        chipCompleted.setOnClickListener(v -> selectFilter(OrderStatus.COMPLETED));
        chipCancelled.setOnClickListener(v -> selectFilter(OrderStatus.CANCELLED));

        selectFilter(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectFilter(selectedFilter);
    }

    private void selectFilter(OrderStatus filter) {
        selectedFilter = filter;
        updateChipStyles();

        List<Order> all = OrderRepository.getOrders(this);
        List<Order> filtered = new ArrayList<>();
        for (Order o : all) {
            if (filter == null || o.status == filter) {
                filtered.add(o);
            }
        }

        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.submit(filtered);
        }
    }

    private void updateChipStyles() {
        updateChip(chipAll, selectedFilter == null);
        updateChip(chipPreparing, selectedFilter == OrderStatus.PREPARING);
        updateChip(chipCompleted, selectedFilter == OrderStatus.COMPLETED);
        updateChip(chipCancelled, selectedFilter == OrderStatus.CANCELLED);
    }

    private void updateChip(TextView chip, boolean isSelected) {
        if (chip == null) return;
        chip.setBackgroundResource(isSelected ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
        chip.setTextColor(getColor(isSelected ? R.color.coffee_surface : R.color.coffee_muted));
    }

    private static class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.Holder> {
        interface OnOrderClick { void onClick(Order order); }
        private final List<Order> items = new ArrayList<>();
        private final OnOrderClick listener;

        OrderHistoryAdapter(OnOrderClick listener) { this.listener = listener; }

        void submit(List<Order> orders) {
            items.clear();
            if (orders != null) items.addAll(orders);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Order order = items.get(position);
            h.code.setText(order.id);
            h.date.setText(order.createdAt);
            h.total.setText(NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(order.total) + "đ");

            StringBuilder summary = new StringBuilder();
            int totalCount = 0;
            if (order.items != null) {
                for (int i = 0; i < order.items.size(); i++) {
                    CartItem ci = order.items.get(i);
                    totalCount += ci.quantity;
                    summary.append(ci.product.name).append(" (x").append(ci.quantity).append(")");
                    if (i < order.items.size() - 1) summary.append(", ");
                }
            }
            h.summary.setText(summary.toString());
            h.itemCount.setText(totalCount + " món");

            if (order.status == OrderStatus.PREPARING) {
                h.statusChip.setText("ĐANG CHUẨN BỊ");
                h.statusChip.setBackgroundColor(Color.parseColor("#FFF3E0"));
                h.statusChip.setTextColor(Color.parseColor("#EF6C00"));
            } else if (order.status == OrderStatus.COMPLETED) {
                h.statusChip.setText("HOÀN THÀNH");
                h.statusChip.setBackgroundColor(Color.parseColor("#E8F5E9"));
                h.statusChip.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                h.statusChip.setText("ĐÃ HỦY");
                h.statusChip.setBackgroundColor(Color.parseColor("#F5F5F5"));
                h.statusChip.setTextColor(Color.parseColor("#757575"));
            }

            h.cardRoot.setOnClickListener(v -> { if (listener != null) listener.onClick(order); });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            final View cardRoot;
            final TextView code, date, statusChip, summary, itemCount, total;

            Holder(View v) {
                super(v);
                cardRoot = v.findViewById(R.id.orderCardRoot);
                code = v.findViewById(R.id.orderCardId);
                date = v.findViewById(R.id.orderCardDate);
                statusChip = v.findViewById(R.id.orderCardStatusChip);
                summary = v.findViewById(R.id.orderCardItemsSummary);
                itemCount = v.findViewById(R.id.orderCardItemCount);
                total = v.findViewById(R.id.orderCardTotal);
            }
        }
    }
}
