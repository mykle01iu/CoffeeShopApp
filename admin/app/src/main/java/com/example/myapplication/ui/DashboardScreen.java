package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.example.myapplication.auth.AccountStore;
import com.example.myapplication.data.ProductRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardScreen {

    private static final String[] LOW_STOCK_ITEMS = {
            "Sữa đặc — còn 2 hộp",
            "Hạt cà phê Arabica — còn 800g"
    };

    private final Activity activity;
    private final Navigator navigator;
    private final AccountStore accounts;
    private final ProductRepository products;

    public DashboardScreen(Activity activity, Navigator navigator,
                           AccountStore accounts, ProductRepository products) {
        this.activity = activity;
        this.navigator = navigator;
        this.accounts = accounts;
        this.products = products;
    }

    public void show() {
        activity.setContentView(R.layout.dashboard);

        TextView date = activity.findViewById(R.id.dashboard_date);
        date.setText(new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));

        TextView productTotal = activity.findViewById(R.id.product_total);
        productTotal.setText(String.valueOf(products.size()));

        populateLowStockAlerts();

        View approvalsButton = activity.findViewById(R.id.open_account_approvals);
        approvalsButton.setVisibility(accounts.isAdmin() ? View.VISIBLE : View.GONE);
        approvalsButton.setOnClickListener(v -> new AccountApprovalDialog(activity, accounts).show());

        activity.findViewById(R.id.revenue_today_card).setOnClickListener(v -> navigator.showRevenue());
        activity.findViewById(R.id.logout_button).setOnClickListener(v -> navigator.logout());        activity.findViewById(R.id.open_products).setOnClickListener(v -> navigator.showProducts());
        activity.findViewById(R.id.open_orders).setOnClickListener(v -> comingSoon());
        activity.findViewById(R.id.open_staff).setOnClickListener(v -> comingSoon());
        activity.findViewById(R.id.open_reports).setOnClickListener(v -> comingSoon());
        activity.findViewById(R.id.open_inventory).setOnClickListener(v -> showInventoryDetails());
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

    private void comingSoon() {
        Toast.makeText(activity, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
    }
}