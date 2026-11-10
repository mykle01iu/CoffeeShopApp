package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderSuccessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        String orderId = getIntent().getStringExtra("orderId");
        Order order = OrderRepository.getOrderById(orderId);

        TextView codeText = findViewById(R.id.orderCodeText);
        TextView totalText = findViewById(R.id.orderTotalText);

        if (order != null) {
            codeText.setText(order.id);
            totalText.setText(NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(order.total) + "đ");
        } else if (orderId != null) {
            codeText.setText(orderId);
        }

        Button btnViewOrders = findViewById(R.id.btnViewOrders);
        btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
