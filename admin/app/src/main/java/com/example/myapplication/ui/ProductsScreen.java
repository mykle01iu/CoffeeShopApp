package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.data.ProductRepository;
import com.example.myapplication.model.Product;

import java.util.Locale;

public class ProductsScreen {

    private final Activity activity;
    private final Navigator navigator;
    private final ProductRepository repo;

    private final int coffee, ink, muted;
    private final int[] filterIds = {R.id.filter_all, R.id.filter_coffee, R.id.filter_tea, R.id.filter_other};
    private final String[] filterNames = {
            ProductRepository.CAT_ALL,
            ProductRepository.CAT_COFFEE,
            ProductRepository.CAT_TEA,
            ProductRepository.CAT_OTHER
    };

    private LinearLayout productList;
    private EditText searchInput;
    private String selectedCategory = ProductRepository.CAT_ALL;

    public ProductsScreen(Activity activity, Navigator navigator, ProductRepository repo) {
        this.activity = activity;
        this.navigator = navigator;
        this.repo = repo;
        this.coffee = activity.getColor(R.color.coffee);
        this.ink = activity.getColor(R.color.ink);
        this.muted = activity.getColor(R.color.muted);
    }

    public void show() {
        activity.setContentView(R.layout.products);

        activity.findViewById(R.id.back_dashboard).setOnClickListener(v -> navigator.showDashboard());
        activity.findViewById(R.id.add_product).setOnClickListener(v ->
                ProductDialog.show(activity, repo, -1, this::renderProducts));

        productList = activity.findViewById(R.id.product_list);
        searchInput = activity.findViewById(R.id.search_products);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderProducts(); }
            @Override public void afterTextChanged(Editable e) { }
        });

        for (int i = 0; i < filterIds.length; i++) {
            final int position = i;
            activity.findViewById(filterIds[i]).setOnClickListener(v -> {
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
            TextView chip = activity.findViewById(filterIds[i]);
            boolean chosen = filterNames[i].equals(selectedCategory);
            chip.setBackgroundResource(chosen ? R.drawable.filter_selected : R.drawable.filter_normal);
            chip.setTextColor(chosen ? Color.WHITE : muted);
        }
    }

    private void renderProducts() {
        if (productList == null) return;
        productList.removeAllViews();

        String query = searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int shown = 0;

        for (int i = 0; i < repo.size(); i++) {
            Product product = repo.get(i);
            if (!selectedCategory.equals(ProductRepository.CAT_ALL)
                    && !product.category.equals(selectedCategory)) continue;
            if (!product.name.toLowerCase(Locale.ROOT).contains(query)) continue;

            shown++;
            productList.addView(createProductRow(product, i));
        }

        TextView count = activity.findViewById(R.id.product_count);
        count.setText(shown + " sản phẩm"
                + (selectedCategory.equals(ProductRepository.CAT_ALL)
                ? " trong thực đơn"
                : " • " + selectedCategory));

        if (shown == 0) {
            TextView empty = new TextView(activity);
            empty.setText("Không tìm thấy sản phẩm phù hợp.");
            empty.setTextColor(muted);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(44), dp(16), dp(44));
            productList.addView(empty);
        }
    }

    private View createProductRow(Product product, int index) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(13), dp(10), dp(13));
        card.setBackgroundResource(R.drawable.card_bg);
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(-1, -2);
        outer.bottomMargin = dp(9);
        card.setLayoutParams(outer);

        TextView icon = new TextView(activity);
        icon.setText(UiUtils.categoryIcon(product.category));
        icon.setTextSize(23);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.icon_badge);
        card.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout details = new LinearLayout(activity);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(12), 0, dp(5), 0);
        card.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

        TextView name = new TextView(activity);
        name.setText(product.name);
        name.setTextColor(ink);
        name.setTextSize(14);
        name.setTypeface(null, Typeface.BOLD);
        details.addView(name);

        TextView category = new TextView(activity);
        category.setText(product.category + "  •  Đang bán");
        category.setTextColor(muted);
        category.setTextSize(11);
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(-2, -2);
        categoryParams.topMargin = dp(4);
        details.addView(category, categoryParams);

        TextView price = new TextView(activity);
        price.setText(UiUtils.formatPrice(product.price));
        price.setTextColor(coffee);
        price.setTextSize(13);
        price.setTypeface(null, Typeface.BOLD);
        details.addView(price);

        TextView edit = new TextView(activity);
        edit.setText("✎");
        edit.setTextSize(18);
        edit.setTextColor(coffee);
        edit.setGravity(Gravity.CENTER);
        card.addView(edit, new LinearLayout.LayoutParams(dp(34), dp(44)));

        edit.setOnClickListener(v -> ProductDialog.show(activity, repo, index, this::renderProducts));
        edit.setOnLongClickListener(v -> {
            ProductDialog.confirmDelete(activity, repo, index, this::renderProducts);
            return true;
        });
        card.setOnClickListener(v -> ProductDialog.show(activity, repo, index, this::renderProducts));
        return card;
    }

    private int dp(int value) {
        return UiUtils.dp(activity, value);
    }
}