package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<Product> products = new ArrayList<>();
    private final String[] lowStockItems = {"Sữa đặc — còn 2 hộp", "Hạt cà phê Arabica — còn 800g"};
    private final int[] filterIds = {R.id.filter_all, R.id.filter_coffee, R.id.filter_tea, R.id.filter_other};
    private final String[] filterNames = {"Tất cả", "Cà phê", "Trà", "Khác"};

    private LinearLayout productList;
    private EditText searchInput;
    private String selectedCategory = "Tất cả";
    private int coffee, ink, muted;
    private OnBackPressedCallback productsBackCallback;

    private TextView tabLogin, tabRegister, authTitle, authSubtitle, loginError, registerError;
    private View loginContainer, registerContainer;

    private static class Product {
        String name, category;
        int price;

        Product(String name, String category, int price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        coffee = getColor(R.color.coffee);
        ink = getColor(R.color.ink);
        muted = getColor(R.color.muted);
        productsBackCallback = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { showDashboard(); }
        };
        getOnBackPressedDispatcher().addCallback(this, productsBackCallback);
        seedProducts();
        showLogin();
    }


    private void showLogin() {
        productsBackCallback.setEnabled(false);
        setContentView(R.layout.login);

        tabLogin = findViewById(R.id.tab_login);
        tabRegister = findViewById(R.id.tab_register);
        loginContainer = findViewById(R.id.login_container);
        registerContainer = findViewById(R.id.register_container);
        authTitle = findViewById(R.id.auth_title);
        authSubtitle = findViewById(R.id.auth_subtitle);
        loginError = findViewById(R.id.login_error);
        registerError = findViewById(R.id.register_error);

        tabLogin.setOnClickListener(v -> showAuthTab(true));
        tabRegister.setOnClickListener(v -> showAuthTab(false));
        findViewById(R.id.login_button).setOnClickListener(v -> login());
        findViewById(R.id.register_button).setOnClickListener(v -> register());
        findViewById(R.id.forgot_password).setOnClickListener(v ->
                Toast.makeText(this, "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu", Toast.LENGTH_SHORT).show());

        showAuthTab(true);
    }

    private void showAuthTab(boolean isLogin) {
        loginContainer.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        registerContainer.setVisibility(isLogin ? View.GONE : View.VISIBLE);

        authTitle.setText(isLogin ? "Chào mừng trở lại" : "Tạo tài khoản mới");
        authSubtitle.setText(isLogin
                ? "Đăng nhập để quản lý cửa hàng của bạn"
                : "Đăng ký để bắt đầu quản lý cửa hàng");

        loginError.setVisibility(View.GONE);
        registerError.setVisibility(View.GONE);

        setTabStyle(tabLogin, isLogin);
        setTabStyle(tabRegister, !isLogin);
    }

    private void setTabStyle(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.filter_selected : R.drawable.filter_normal);
        tab.setTextColor(selected ? Color.WHITE : muted);
    }

    private void login() {
        String email = textOf(R.id.email_input);
        String password = textOf(R.id.password_input);

        if (email.isEmpty() || password.isEmpty()) {
            showError(loginError, "Vui lòng nhập email và mật khẩu.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(loginError, "Email không hợp lệ.");
            return;
        }
        showDashboard();
    }

    private void register() {
        String name = textOf(R.id.name_input);
        String phone = textOf(R.id.phone_input);
        String email = textOf(R.id.reg_email_input);
        String password = textOf(R.id.reg_password_input);
        String confirm = textOf(R.id.confirm_password_input);

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError(registerError, "Vui lòng điền đầy đủ thông tin.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(registerError, "Email không hợp lệ.");
            return;
        }
        if (password.length() < 8) {
            showError(registerError, "Mật khẩu phải có ít nhất 8 ký tự.");
            return;
        }
        if (!password.equals(confirm)) {
            showError(registerError, "Mật khẩu xác nhận không khớp.");
            return;
        }

        Toast.makeText(this, "Đã gửi đăng ký, vui lòng chờ quản trị viên xác nhận", Toast.LENGTH_LONG).show();
        clearInputs(R.id.name_input, R.id.phone_input,
                R.id.reg_email_input, R.id.reg_password_input, R.id.confirm_password_input);
        showAuthTab(true);
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private String textOf(int id) {
        return ((EditText) findViewById(id)).getText().toString().trim();
    }

    private void clearInputs(int... ids) {
        for (int id : ids) {
            ((EditText) findViewById(id)).setText("");
        }
    }


    private void showDashboard() {
        productsBackCallback.setEnabled(false);
        setContentView(R.layout.dashboard);
        productList = null;
        searchInput = null;

        TextView date = findViewById(R.id.dashboard_date);
        date.setText(new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));

        TextView productTotal = findViewById(R.id.product_total);
        productTotal.setText(String.valueOf(products.size()));
        populateLowStockAlerts();

        findViewById(R.id.revenue_today_card).setOnClickListener(v -> showTodaySalesDetails());
        findViewById(R.id.logout_button).setOnClickListener(v -> showLogin());
        findViewById(R.id.open_products).setOnClickListener(v -> showProducts());
        findViewById(R.id.open_orders).setOnClickListener(v -> comingSoon());
        findViewById(R.id.open_staff).setOnClickListener(v -> comingSoon());
        findViewById(R.id.open_reports).setOnClickListener(v -> comingSoon());
        findViewById(R.id.open_inventory).setOnClickListener(v -> showInventoryDetails());
    }

    private void populateLowStockAlerts() {
        LinearLayout alerts = findViewById(R.id.low_stock_list);
        alerts.removeAllViews();
        for (int i = 0; i < lowStockItems.length; i++) {
            TextView item = new TextView(this);
            item.setText(lowStockItems[i]);
            item.setTextColor(Color.rgb(180, 35, 24));
            item.setTextSize(13);
            item.setTypeface(null, Typeface.BOLD);
            if (i > 0) item.setPadding(0, dp(6), 0, 0);
            alerts.addView(item);
        }
    }

    private void showInventoryDetails() {
        StringBuilder details = new StringBuilder();
        for (String item : lowStockItems) details.append("• ").append(item).append("\n");
        new AlertDialog.Builder(this)
                .setTitle("Kho · Cảnh báo tồn kho")
                .setMessage(details.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void comingSoon() {
        Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void showTodaySalesDetails() {
        showRevenueDetails();
    }

    private void showRevenueDetails() {
        setContentView(R.layout.revenue_details);
        productsBackCallback.setEnabled(true);
        findViewById(R.id.back_revenue_dashboard).setOnClickListener(v -> showDashboard());
    }


    private void showProducts() {
        setContentView(R.layout.products);
        productsBackCallback.setEnabled(true);
        findViewById(R.id.back_dashboard).setOnClickListener(v -> showDashboard());
        findViewById(R.id.add_product).setOnClickListener(v -> showProductDialog(-1));

        productList = findViewById(R.id.product_list);
        searchInput = findViewById(R.id.search_products);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderProducts(); }
            @Override public void afterTextChanged(Editable e) { }
        });

        for (int i = 0; i < filterIds.length; i++) {
            final int position = i;
            findViewById(filterIds[i]).setOnClickListener(v -> {
                selectedCategory = filterNames[position];
                updateFilterChips();
                renderProducts();
            });
        }

        updateFilterChips();
        renderProducts();
    }

    private void updateFilterChips() {
        for (int i = 0; i < filterIds.length; i++) {
            TextView chip = findViewById(filterIds[i]);
            boolean chosen = filterNames[i].equals(selectedCategory);
            chip.setBackgroundResource(chosen ? R.drawable.filter_selected : R.drawable.filter_normal);
            chip.setTextColor(chosen ? Color.WHITE : muted);
        }
    }

    private void seedProducts() {
        products.add(new Product("Cà phê sữa đá", "Cà phê", 35000));
        products.add(new Product("Bạc xỉu", "Cà phê", 39000));
        products.add(new Product("Americano", "Cà phê", 42000));
        products.add(new Product("Cappuccino", "Cà phê", 55000));
        products.add(new Product("Trà đào cam sả", "Trà", 49000));
        products.add(new Product("Trà sen vàng", "Trà", 45000));
        products.add(new Product("Trà vải", "Trà", 46000));
        products.add(new Product("Bánh croissant", "Khác", 32000));
    }

    private void renderProducts() {
        if (productList == null) return;
        productList.removeAllViews();

        String query = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int shown = 0;

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (!selectedCategory.equals("Tất cả") && !product.category.equals(selectedCategory)) continue;
            if (!product.name.toLowerCase(Locale.ROOT).contains(query)) continue;
            shown++;
            productList.addView(createProductRow(product, i));
        }

        TextView count = findViewById(R.id.product_count);
        count.setText(shown + " sản phẩm"
                + (selectedCategory.equals("Tất cả") ? " trong thực đơn" : " • " + selectedCategory));

        if (shown == 0) {
            TextView empty = new TextView(this);
            empty.setText("Không tìm thấy sản phẩm phù hợp.");
            empty.setTextColor(muted);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(44), dp(16), dp(44));
            productList.addView(empty);
        }
    }

    private View createProductRow(Product product, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(13), dp(10), dp(13));
        card.setBackgroundResource(R.drawable.card_bg);
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(-1, -2);
        outer.bottomMargin = dp(9);
        card.setLayoutParams(outer);

        TextView icon = new TextView(this);
        icon.setText(categoryIcon(product.category));
        icon.setTextSize(23);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.icon_badge);
        card.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(12), 0, dp(5), 0);
        card.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

        TextView name = new TextView(this);
        name.setText(product.name);
        name.setTextColor(ink);
        name.setTextSize(14);
        name.setTypeface(null, Typeface.BOLD);
        details.addView(name);

        TextView category = new TextView(this);
        category.setText(product.category + "  •  Đang bán");
        category.setTextColor(muted);
        category.setTextSize(11);
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(-2, -2);
        categoryParams.topMargin = dp(4);
        details.addView(category, categoryParams);

        TextView price = new TextView(this);
        price.setText(formatPrice(product.price));
        price.setTextColor(coffee);
        price.setTextSize(13);
        price.setTypeface(null, Typeface.BOLD);
        details.addView(price);

        TextView edit = new TextView(this);
        edit.setText("✎");
        edit.setTextSize(18);
        edit.setTextColor(coffee);
        edit.setGravity(Gravity.CENTER);
        card.addView(edit, new LinearLayout.LayoutParams(dp(34), dp(44)));

        edit.setOnClickListener(v -> showProductDialog(index));
        edit.setOnLongClickListener(v -> {
            confirmDelete(index);
            return true;
        });
        card.setOnClickListener(v -> showProductDialog(index));
        return card;
    }

    private String categoryIcon(String category) {
        switch (category) {
            case "Cà phê": return "☕";
            case "Trà": return "🍵";
            default: return "🥐";
        }
    }

    private String formatPrice(int price) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price) + " ₫";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showProductDialog(int index) {
        boolean editing = index >= 0;
        Product old = editing ? products.get(index) : null;

        LinearLayout form = new LinearLayout(this);
        form.setPadding(dp(22), dp(8), dp(22), 0);
        form.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Tên sản phẩm");
        name.setText(editing ? old.name : "");
        form.addView(name);

        EditText price = new EditText(this);
        price.setSingleLine(true);
        price.setHint("Giá bán (VNĐ)");
        price.setInputType(InputType.TYPE_CLASS_NUMBER);
        price.setText(editing ? String.valueOf(old.price) : "");
        form.addView(price);

        EditText category = new EditText(this);
        category.setSingleLine(true);
        category.setHint("Danh mục: Cà phê, Trà hoặc Khác");
        category.setText(editing ? old.category : "Cà phê");
        form.addView(category);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Cập nhật sản phẩm" : "Thêm sản phẩm")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu thay đổi" : "Thêm mới", null)
                .setNeutralButton(editing ? "Xóa" : null, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String n = name.getText().toString().trim();
                String c = category.getText().toString().trim();
                String priceText = price.getText().toString().trim();

                if (n.isEmpty() || c.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int p;
                try {
                    p = Integer.parseInt(priceText);
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, "Giá không hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (p <= 0) {
                    Toast.makeText(this, "Giá phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editing) {
                    old.name = n;
                    old.price = p;
                    old.category = c;
                } else {
                    products.add(0, new Product(n, c, p));
                }
                renderProducts();
                dialog.dismiss();
                Toast.makeText(this, editing ? "Đã cập nhật sản phẩm" : "Đã thêm sản phẩm", Toast.LENGTH_SHORT).show();
            });

            if (editing) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    dialog.dismiss();
                    confirmDelete(index);
                });
            }
        });
        dialog.show();
    }

    private void confirmDelete(int index) {
        if (index < 0 || index >= products.size()) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm?")
                .setMessage("Bạn muốn xóa “" + products.get(index).name + "” khỏi thực đơn?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    products.remove(index);
                    renderProducts();
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
