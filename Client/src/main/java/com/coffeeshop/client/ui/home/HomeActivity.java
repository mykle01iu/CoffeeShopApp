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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends Activity {
    private final List<DrinkProduct> products = MenuCatalog.getProducts();
    private final Map<String, Integer> selectedDrinks = new LinkedHashMap<>();
    private final Map<String, Boolean> favorites = new LinkedHashMap<>();
    private ScrollView homeScrollView;
    private HorizontalScrollView productsScrollView;
    private LinearLayout productCards;
    private View productsSection;
    private View emptyProducts;
    private SharedPreferences homePreferences;
    private String selectedCategory = "all";
    private boolean favoritesOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.homeBrandName)).setTypeface(FontUtils.lora(this, 600));
        ((TextView) findViewById(R.id.promoTitle)).setTypeface(FontUtils.lora(this, 600));
        ((TextView) findViewById(R.id.popularTitle)).setTypeface(FontUtils.lora(this, 600));

        homeScrollView = findViewById(R.id.homeScrollView);
        productsScrollView = findViewById(R.id.productsScrollView);
        productCards = findViewById(R.id.productCards);
        productsSection = findViewById(R.id.popularTitle);
        emptyProducts = findViewById(R.id.homeEmptyProducts);
        homePreferences = getSharedPreferences("moc_mien_home", MODE_PRIVATE);

        loadSavedState();

        setupCategories();
        setupSearch();

        findViewById(R.id.promoOrderButton).setOnClickListener(view -> scrollToProducts());
        findViewById(R.id.popularSeeAll).setOnClickListener(view -> {
            favoritesOnly = false;
            updateProducts(currentQuery());
            scrollToProducts();
        });

        View.OnClickListener openCart = view -> showSelectedDrinks();
        findViewById(R.id.homeCartButton).setOnClickListener(openCart);
        findViewById(R.id.navOrdersItem).setOnClickListener(openCart);

        View.OnClickListener openAccount = view -> startActivity(new Intent(this, ProfileActivity.class));
        findViewById(R.id.homeAddressRow).setOnClickListener(view ->
                startActivity(new Intent(this, AddressActivity.class)));
        findViewById(R.id.navProfileItem).setOnClickListener(openAccount);

        View navHome = findViewById(R.id.navHomeItem);
        View navMenu = findViewById(R.id.navMenuItem);
        navHome.setOnClickListener(view -> {
            setSelectedNavigation(navHome);
            homeScrollView.smoothScrollTo(0, 0);
        });
        navMenu.setOnClickListener(view -> startActivity(new Intent(this, MenuActivity.class)));

        updateProducts("");
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void loadSavedState() {
        if (homePreferences.contains("favorite_iced")) {
            homePreferences.edit().putBoolean("favorite_iced_milk_coffee", homePreferences.getBoolean("favorite_iced", false)).remove("favorite_iced").apply();
        }
        if (homePreferences.contains("favorite_latte")) {
            homePreferences.edit().putBoolean("favorite_caramel_latte", homePreferences.getBoolean("favorite_latte", false)).remove("favorite_latte").apply();
        }
        if (homePreferences.contains("cart_iced")) {
            homePreferences.edit().putInt("cart_iced_milk_coffee", homePreferences.getInt("cart_iced", 0)).remove("cart_iced").apply();
        }
        if (homePreferences.contains("cart_latte")) {
            homePreferences.edit().putInt("cart_caramel_latte", homePreferences.getInt("cart_latte", 0)).remove("cart_latte").apply();
        }
        for (DrinkProduct product : products) {
            favorites.put(product.id, homePreferences.getBoolean("favorite_" + product.id, false));
            int count = homePreferences.getInt("cart_" + product.id, 0);
            if (count > 0) selectedDrinks.put(product.id, count);
        }
    }

    private void setupCategories() {
        TextView all = findViewById(R.id.categoryAll);
        TextView coffee = findViewById(R.id.categoryCoffee);
        TextView tea = findViewById(R.id.categoryTea);
        TextView smoothie = findViewById(R.id.categorySmoothie);
        TextView snacks = findViewById(R.id.categorySnacks);

        all.setOnClickListener(view -> selectCategory("all", all));
        coffee.setOnClickListener(view -> selectCategory("coffee", coffee));
        tea.setOnClickListener(view -> selectCategory("tea", tea));
        smoothie.setOnClickListener(view -> selectCategory("smoothie", smoothie));
        snacks.setOnClickListener(view -> selectCategory("snack", snacks));
    }

    private void setupSearch() {
        EditText search = findViewById(R.id.searchInput);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateProducts(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private String currentQuery() {
        return ((EditText) findViewById(R.id.searchInput)).getText().toString();
    }

    private void scrollToProducts() {
        homeScrollView.post(() -> {
            int targetY = 0;
            View current = productsSection;
            while (current != homeScrollView && current != null) {
                targetY += current.getTop();
                if (current.getParent() instanceof View) current = (View) current.getParent();
                else current = null;
            }
            homeScrollView.smoothScrollTo(0, targetY);
        });
    }

    private void selectCategory(String category, TextView selectedChip) {
        selectedCategory = category;
        int[] ids = {R.id.categoryAll, R.id.categoryCoffee, R.id.categoryTea, R.id.categorySmoothie, R.id.categorySnacks};
        for (int id : ids) {
            TextView chip = findViewById(id);
            boolean selected = chip == selectedChip;
            chip.setBackgroundResource(selected ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
            chip.setTextColor(getColor(selected ? R.color.coffee_surface : R.color.coffee_muted));
            chip.setTypeface(FontUtils.inter(this, selected ? 600 : 500));
        }
        updateProducts(currentQuery());
    }

    private void updateProducts(String query) {
        String normalizedQuery = normalize(query);
        List<DrinkProduct> visible = new ArrayList<>();
        for (DrinkProduct product : products) {
            boolean categoryMatch = selectedCategory.equals("all") || selectedCategory.equals(product.category);
            boolean queryMatch = normalizedQuery.isEmpty()
                    || normalize(product.name).contains(normalizedQuery)
                    || normalize(product.description).contains(normalizedQuery);
            boolean favoriteMatch = !favoritesOnly || Boolean.TRUE.equals(favorites.get(product.id));
            if (categoryMatch && queryMatch && favoriteMatch) visible.add(product);
        }

        productCards.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        float contentWidthDp = getResources().getDisplayMetrics().widthPixels / density - 48f;
        int cardWidth = Math.round(Math.max(160f, (contentWidthDp - 12f) / 2f) * density);
        for (int index = 0; index < visible.size(); index++) {
            DrinkProduct product = visible.get(index);
            View card = LayoutInflater.from(this).inflate(R.layout.item_drink, productCards, false);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (index < visible.size() - 1) params.rightMargin = Math.round(12 * density);
            card.setLayoutParams(params);
            bindProductCard(card, product);
            productCards.addView(card);
        }

        boolean noResults = visible.isEmpty();
        productsScrollView.setVisibility(noResults ? View.GONE : View.VISIBLE);
        emptyProducts.setVisibility(noResults ? View.VISIBLE : View.GONE);
        TextView emptyText = (TextView) emptyProducts;
        if (favoritesOnly && normalizedQuery.isEmpty()) emptyText.setText(R.string.home_favorites_empty);
        else emptyText.setText(normalizedQuery.isEmpty() ? R.string.home_category_empty : R.string.home_search_empty);
    }

    private void bindProductCard(View card, DrinkProduct product) {
        ImageView image = card.findViewById(R.id.drinkImage);
        TextView rating = card.findViewById(R.id.drinkRating);
        TextView name = card.findViewById(R.id.drinkName);
        TextView description = card.findViewById(R.id.drinkDescription);
        TextView price = card.findViewById(R.id.drinkPrice);
        ImageButton favorite = card.findViewById(R.id.favoriteDrinkButton);
        TextView add = card.findViewById(R.id.addDrinkButton);

        image.setImageResource(product.imageRes);
        image.setContentDescription(product.name);
        rating.setText(getString(R.string.home_rating_format, product.rating));
        name.setText(product.name);
        name.setTypeface(FontUtils.inter(this, 600));
        description.setText(product.description);
        price.setText(String.format(Locale.getDefault(), "%,dđ", product.priceVnd));
        price.setTypeface(FontUtils.inter(this, 700));

        updateFavoriteIcon(favorite, product);
        favorite.setOnClickListener(view -> toggleFavorite(product));
        add.setContentDescription(getString(R.string.home_add_product, product.name));
        add.setOnClickListener(view -> addDrink(product));
        card.setOnClickListener(view -> addDrink(product));
    }

    private void updateCartBadge() {
        int total = 0;
        for (int itemCount : selectedDrinks.values()) total += itemCount;
        TextView badge = findViewById(R.id.cartBadgeText);
        if (badge != null) {
            badge.setText(String.valueOf(total > 0 ? total : 2));
        }
    }

    private void updateFavoriteIcon(ImageButton button, DrinkProduct product) {
        boolean isFavorite = Boolean.TRUE.equals(favorites.get(product.id));
        button.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        button.setContentDescription(getString(isFavorite ? R.string.home_favorite_removed : R.string.home_favorite_added, product.name));
    }

    private void addDrink(DrinkProduct product) {
        int count = selectedDrinks.containsKey(product.id) ? selectedDrinks.get(product.id) + 1 : 1;
        selectedDrinks.put(product.id, count);
        homePreferences.edit().putInt("cart_" + product.id, count).apply();
        updateCartBadge();
        int totalCount = 0;
        for (int itemCount : selectedDrinks.values()) totalCount += itemCount;
        Toast.makeText(this, getString(R.string.home_added_to_cart, product.name, totalCount), Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite(DrinkProduct product) {
        boolean isFavorite = !Boolean.TRUE.equals(favorites.get(product.id));
        favorites.put(product.id, isFavorite);
        homePreferences.edit().putBoolean("favorite_" + product.id, isFavorite).apply();
        Toast.makeText(this, getString(isFavorite ? R.string.home_favorite_added : R.string.home_favorite_removed, product.name), Toast.LENGTH_SHORT).show();
        updateProducts(currentQuery());
    }

    private void showSelectedDrinks() {
        if (selectedDrinks.isEmpty()) {
            new AlertDialog.Builder(this).setTitle(R.string.nav_orders).setMessage(R.string.home_empty_cart)
                    .setPositiveButton(R.string.home_view_menu, (dialog, which) -> scrollToProducts())
                    .setNegativeButton(android.R.string.cancel, null).show();
            return;
        }
        StringBuilder summary = new StringBuilder();
        int total = 0;
        for (Map.Entry<String, Integer> entry : selectedDrinks.entrySet()) {
            DrinkProduct product = findProduct(entry.getKey());
            if (product == null) continue;
            if (summary.length() > 0) summary.append('\n');
            summary.append(product.name).append(" × ").append(entry.getValue());
            total += product.priceVnd * entry.getValue();
        }
        summary.append("\n\n").append(getString(R.string.home_favorite_subtotal, String.format(Locale.getDefault(), "%,dđ", total)));
        new AlertDialog.Builder(this).setTitle(R.string.home_cart_title).setMessage(summary.toString())
                .setPositiveButton(R.string.home_view_menu, (dialog, which) -> scrollToProducts())
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private DrinkProduct findProduct(String id) {
        for (DrinkProduct product : products) if (product.id.equals(id)) return product;
        return null;
    }

    private String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd').trim();
    }

    private void setSelectedNavigation(View selectedItem) {
        int[] ids = {R.id.navHomeItem, R.id.navMenuItem, R.id.navOrdersItem, R.id.navProfileItem};
        for (int id : ids) {
            LinearLayout item = findViewById(id);
            if (item == null) continue;
            boolean selected = item == selectedItem;
            View pill = item.findViewById(R.id.navHomePill);
            if (pill != null) {
                pill.setBackgroundResource(selected ? R.drawable.bg_nav_tab_selected : 0);
            }
            updateChildColors(item, selected);
        }
    }

    private void updateChildColors(ViewGroup group, boolean selected) {
        for (int index = 0; index < group.getChildCount(); index++) {
            View child = group.getChildAt(index);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(getColor(selected ? R.color.coffee_brown : R.color.coffee_muted));
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(getColor(selected ? R.color.coffee_brown : R.color.coffee_muted));
                ((TextView) child).setTypeface(FontUtils.inter(this, selected ? 600 : 400));
            } else if (child instanceof ViewGroup) {
                updateChildColors((ViewGroup) child, selected);
            }
        }
    }
}
