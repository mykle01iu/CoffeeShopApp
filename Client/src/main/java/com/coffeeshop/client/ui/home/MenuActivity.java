package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.CartManager;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuActivity extends Activity {
    private final List<DrinkProduct> products = MenuCatalog.getProducts();
    private final Map<String, Boolean> favorites = new LinkedHashMap<>();
    private final Map<String, Integer> cart = new LinkedHashMap<>();
    private SharedPreferences preferences;
    private SharedPreferences cartPreferences;
    private LinearLayout productRows;
    private TextView resultCount;
    private TextView emptyState;
    private String category = "all";
    private TextView selectedChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        findViewById(R.id.menuTitle).setFocusable(true);
        ((TextView) findViewById(R.id.menuTitle)).setTypeface(FontUtils.lora(this, 500));
        findViewById(R.id.openCatalogList).setOnClickListener(view ->
                startActivity(new Intent(this, com.coffeeshop.client.catalog.ui.ProductListActivity.class)));
        preferences = getSharedPreferences("moc_mien_home", MODE_PRIVATE);
        cartPreferences = CartRepository.getCartPreferences(this);
        productRows = findViewById(R.id.menuProductRows);
        resultCount = findViewById(R.id.menuResultCount);
        emptyState = findViewById(R.id.menuEmptyState);
        loadState();
        createCategories();
        setupSearch();
        renderProducts("");
        setupNavigation();
    }

    private void loadState() {
        CartOptions.migrateOldCart(cartPreferences, products);
        for (DrinkProduct product : products) {
            favorites.put(product.id, preferences.getBoolean("favorite_" + product.id, false));
            for (CartOptions.Selection option : CartOptions.selections(cartPreferences, product)) {
                cart.put(product.id + "|" + option.token(), option.quantity);
            }
        }
    }

    private void createCategories() {
        LinearLayout container = findViewById(R.id.menuCategoryContainer);
        String[] keys = {"all", "coffee", "tea", "smoothie", "latte", "snack"};
        int[] labels = {R.string.category_all, R.string.category_coffee, R.string.category_tea,
                R.string.category_smoothie, R.string.category_latte, R.string.category_snacks};
        for (int i = 0; i < keys.length; i++) {
            TextView chip = new TextView(this);
            chip.setText(labels[i]);
            chip.setTextSize(13);
            chip.setTypeface(FontUtils.inter(this, 600));
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setPadding(dp(16), 0, dp(16), 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
            params.setMarginEnd(dp(8));
            container.addView(chip, params);
            String key = keys[i];
            chip.setOnClickListener(view -> selectCategory(key, chip));
            if (i == 0) selectedChip = chip;
        }
        updateChipStyles();
    }

    private void selectCategory(String value, TextView chip) {
        category = value;
        selectedChip = chip;
        updateChipStyles();
        renderProducts(currentQuery());
    }

    private void updateChipStyles() {
        LinearLayout container = findViewById(R.id.menuCategoryContainer);
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            boolean active = chip == selectedChip;
            chip.setBackgroundResource(active ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
            chip.setTextColor(getColor(active ? R.color.coffee_surface : R.color.coffee_muted));
        }
    }

    private void setupSearch() {
        EditText input = findViewById(R.id.menuSearchInput);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderProducts(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private String currentQuery() {
        return ((EditText) findViewById(R.id.menuSearchInput)).getText().toString();
    }

    private void renderProducts(String query) {
        String normalized = normalize(query);
        List<DrinkProduct> visible = new ArrayList<>();
        for (DrinkProduct product : products) {
            boolean matchesCategory = category.equals("all") || product.category.equals(category);
            boolean matchesQuery = normalized.isEmpty() || normalize(product.name).contains(normalized)
                    || normalize(product.description).contains(normalized);
            if (matchesCategory && matchesQuery) visible.add(product);
        }
        productRows.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int cardWidth = Math.round((getResources().getDisplayMetrics().widthPixels / density - 60f) / 2f * density);
        for (int i = 0; i < visible.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) rowParams.topMargin = dp(12);
            productRows.addView(row, rowParams);
            bindCard(row, visible.get(i), cardWidth);
            if (i + 1 < visible.size()) bindCard(row, visible.get(i + 1), cardWidth);
        }
        resultCount.setText(getString(R.string.menu_result_count, visible.size()));
        boolean empty = visible.isEmpty();
        productRows.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        emptyState.setText(normalized.isEmpty() ? R.string.home_category_empty : R.string.home_search_empty);
    }

    private void bindCard(LinearLayout row, DrinkProduct product, int width) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_drink, row, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (row.getChildCount() == 0) params.setMarginEnd(dp(12));
        row.addView(card, params);
        ((ImageView) card.findViewById(R.id.drinkImage)).setImageResource(product.imageRes);
        ((ImageView) card.findViewById(R.id.drinkImage)).setContentDescription(product.name);
        ((TextView) card.findViewById(R.id.drinkRating)).setText(getString(R.string.home_rating_format, product.rating));
        TextView name = card.findViewById(R.id.drinkName);
        name.setText(product.name);
        name.setTypeface(FontUtils.inter(this, 600));
        ((TextView) card.findViewById(R.id.drinkDescription)).setText(product.description);
        TextView price = card.findViewById(R.id.drinkPrice);
        price.setText(String.format(Locale.getDefault(), "%,d\u0111", CartOptions.price(product, "S")));
        price.setTypeface(FontUtils.inter(this, 700));
        ImageButton favorite = card.findViewById(R.id.favoriteDrinkButton);
        updateFavoriteIcon(favorite, product);
        favorite.setOnClickListener(view -> toggleFavorite(product));
        TextView add = card.findViewById(R.id.addDrinkButton);
        add.setContentDescription(getString(R.string.home_add_product, product.name));
        add.setOnClickListener(view -> addToCart(product));
        card.setOnClickListener(view -> addToCart(product));
    }

    private void updateFavoriteIcon(ImageButton button, DrinkProduct product) {
        boolean favorite = Boolean.TRUE.equals(this.favorites.get(product.id));
        button.setImageResource(favorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        button.setContentDescription(getString(favorite ? R.string.home_favorite_removed : R.string.home_favorite_added,
                product.name));
    }

    private void toggleFavorite(DrinkProduct product) {
        boolean value = !Boolean.TRUE.equals(favorites.get(product.id));
        favorites.put(product.id, value);
        preferences.edit().putBoolean("favorite_" + product.id, value).apply();
        Toast.makeText(this, getString(value ? R.string.home_favorite_added : R.string.home_favorite_removed,
                product.name), Toast.LENGTH_SHORT).show();
        renderProducts(currentQuery());
    }

    private void addToCart(DrinkProduct product) {
        int id;
        try { id=Integer.parseInt(product.id); } catch(Exception ignored) { id=MenuCatalog.getProducts().indexOf(product)+1; }
        for(com.coffeeshop.client.catalog.model.Product item:com.coffeeshop.client.catalog.data.ProductRepository.getProducts()) {
            if(item.id!=id) continue;
            ProductOptionsSheet.show(this,item,null,(options,quantity)->{
                CartRepository.add(this,item,options,quantity); loadState();
                String size=options.size==null?getString(R.string.options_no_size):options.size.label;
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),getString(R.string.options_added_snackbar,item.name,size,CartRepository.totalQuantity(this)),com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            });
            return;
        }
    }
    private void setupNavigation() {
        findViewById(R.id.navHomeItem).setOnClickListener(view -> {
            finish();
        });
        findViewById(R.id.navMenuItem).setOnClickListener(view -> ((ScrollView) findViewById(R.id.menuScrollView)).smoothScrollTo(0, 0));
        findViewById(R.id.navOrdersItem).setOnClickListener(view -> startActivity(new Intent(this, OrdersActivity.class)));
        View.OnClickListener profile = view -> startActivity(new Intent(this, ProfileActivity.class));
        findViewById(R.id.navProfileItem).setOnClickListener(profile);
    }

    private void showCart() {
        if (cart.isEmpty()) {
            new AlertDialog.Builder(this).setTitle(R.string.nav_orders).setMessage(R.string.home_empty_cart)
                    .setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        StringBuilder summary = new StringBuilder();
        int total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            for (DrinkProduct product : products) if (product.id.equals(parts[0])) {
                CartOptions.Selection option = parts.length > 1 ? CartOptions.parseToken(parts[1]) : null;
                if (option == null) continue;
                if (summary.length() > 0) summary.append('\n');
                summary.append(product.name).append(" \u00b7 ").append(CartOptions.description(option)).append(" \u00d7 ").append(entry.getValue());
                total += CartOptions.price(product, option) * entry.getValue();
            }
        }
        summary.append("\n\n").append(getString(R.string.home_favorite_subtotal,
                String.format(Locale.getDefault(), "%,d\u0111", total)));
        new AlertDialog.Builder(this).setTitle(R.string.home_cart_title).setMessage(summary.toString())
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('\u0111', 'd').trim();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
