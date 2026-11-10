package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.data.CartManager;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.catalog.data.ProductRepository;
import com.google.android.material.snackbar.Snackbar;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.data.FavoriteRepository;
import com.coffeeshop.client.data.MockData;
import com.coffeeshop.client.R;
import com.coffeeshop.client.model.Category;
import com.coffeeshop.client.model.Product;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.coffeeshop.client.adapter.CategoryAdapter;
import com.coffeeshop.client.ui.home.adapter.ProductAdapter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends Activity {
    private final List<DrinkProduct> products = MenuCatalog.getProducts();
    private final List<Product> homeProducts = MockData.getProducts();
    private final Map<String, Integer> selectedDrinks = new LinkedHashMap<>();
    private final Map<String, Boolean> favorites = new LinkedHashMap<>();
    private ScrollView homeScrollView;
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
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

        if (savedInstanceState != null) selectedCategory = savedInstanceState.getString("home.selectedCategory", "all");

        homeScrollView = findViewById(R.id.homeScrollView);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        productsSection = findViewById(R.id.popularTitle);
        emptyProducts = findViewById(R.id.homeEmptyProducts);
        homePreferences = getSharedPreferences("moc_mien_home", MODE_PRIVATE);

        loadSavedState();

        RecyclerView categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        categoryRecyclerView.setHasFixedSize(true);
        categoryAdapter = new CategoryAdapter(this, MockData.getCategories(), this::selectCategory);
        categoryRecyclerView.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(homeProducts, dp(200), dp(14), new ProductAdapter.OnProductActionListener() {
            @Override
            public boolean isFavorite(Product product) {
                return isProductFavorite(product);
            }

            @Override
            public void onProductClick(Product product) {
                DrinkProduct source = findDrinkProduct(product.getId());
                if (source != null) addDrink(source);
            }

            @Override
            public void onFavoriteClick(Product product) {
                DrinkProduct source = findDrinkProduct(product.getId());
                if (source != null) toggleFavorite(source);
            }
        });
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        productsRecyclerView.setHasFixedSize(true);
        productsRecyclerView.setAdapter(productAdapter);
        setupSearch();

        findViewById(R.id.promoOrderButton).setOnClickListener(view -> scrollToProducts());
        findViewById(R.id.popularSeeAll).setOnClickListener(view -> {
            favoritesOnly = false;
            updateProducts(currentQuery());
            scrollToProducts();
        });

        View.OnClickListener openCart = view -> startActivity(new Intent(this, CartActivity.class));
        findViewById(R.id.homeCartButton).setOnClickListener(openCart);

        View.OnClickListener openOrders = view -> startActivity(new Intent(this, OrdersActivity.class));
        findViewById(R.id.navOrdersItem).setOnClickListener(openOrders);

        View.OnClickListener openAccount = view -> startActivity(new Intent(this, ProfileActivity.class));
        findViewById(R.id.homeAddressRow).setOnClickListener(view -> {
            Address current = AddressRepository.get(this).getDefault();
            AddressDialogs.showPicker(this, current, selected -> {
                TextView description = findViewById(R.id.homeAddressDescription);
                if (description != null) description.setText(getString(R.string.address_home_delivery_prefix, selected.street, selected.district));
            });
        });
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
        Address defaultAddress = AddressRepository.get(this).getDefault();
        TextView addressDescription = findViewById(R.id.homeAddressDescription);
        if (addressDescription != null && defaultAddress != null) {
            addressDescription.setText(getString(R.string.address_home_delivery_prefix, defaultAddress.street, defaultAddress.district));
        } else if (addressDescription != null) {
            addressDescription.setText(R.string.home_address_description);
        }
        updateCartBadge();
        updateProducts(currentQuery());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("home.selectedCategory", selectedCategory);
        super.onSaveInstanceState(outState);
    }

    private void loadSavedState() {
        selectedDrinks.clear();
        for (DrinkProduct product : products) {
            int count = homePreferences.getInt("cart_" + product.id, 0);
            if (count > 0) selectedDrinks.put(product.id, count);
        }
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

    private void selectCategory(Category category) {
        selectedCategory = category.getId();
        updateProducts(currentQuery());
    }

    private void updateProducts(String query) {
        String normalizedQuery = normalize(query);
        List<Product> visible = new ArrayList<>();
        for (Product product : homeProducts) {
            boolean categoryMatch = selectedCategory.equals("all") || selectedCategory.equals(product.getCategoryId());
            boolean queryMatch = normalizedQuery.isEmpty()
                    || normalize(product.getName()).contains(normalizedQuery)
                    || normalize(product.getDescription()).contains(normalizedQuery);
            boolean favoriteMatch = !favoritesOnly || Boolean.TRUE.equals(favorites.get(product.getId()));
            if (categoryMatch && queryMatch && favoriteMatch) visible.add(product);
        }
        productAdapter.updateData(visible);

        boolean noResults = visible.isEmpty();
        productsRecyclerView.setVisibility(noResults ? View.GONE : View.VISIBLE);
        emptyProducts.setVisibility(noResults ? View.VISIBLE : View.GONE);
        TextView emptyText = (TextView) emptyProducts;
        if (favoritesOnly && normalizedQuery.isEmpty()) emptyText.setText(R.string.home_favorites_empty);
        else emptyText.setText(normalizedQuery.isEmpty() ? R.string.home_category_empty : R.string.home_search_empty);
    }

    private void updateCartBadge() {
        int totalCount = CartManager.getTotalItemCount(this);
        TextView badge = findViewById(R.id.cartBadgeText);
        if (badge != null) {
            if (totalCount == 0) {
                badge.setVisibility(View.GONE);
            } else if (totalCount > 99) {
                badge.setText("99+");
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setText(String.valueOf(totalCount));
                badge.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isProductFavorite(Product product) {
        if (product == null) return false;
        int numId = parseNumericId(product.getId());
        return FavoriteRepository.isFavorite(this, numId) || Boolean.TRUE.equals(favorites.get(product.getId()));
    }

    private void updateFavoriteIcon(ImageButton button, DrinkProduct product) {
        int numId = parseNumericId(product.id);
        boolean isFavorite = FavoriteRepository.isFavorite(this, numId) || Boolean.TRUE.equals(favorites.get(product.id));
        button.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        button.setColorFilter(Color.parseColor(isFavorite ? "#E53935" : "#7D6C61"));
        button.setContentDescription(getString(isFavorite ? R.string.home_favorite_removed : R.string.home_favorite_added, product.name));
    }

    private void addDrink(DrinkProduct product) {
        if (product == null) return;
        int id = parseNumericId(product.id);
        for (com.coffeeshop.client.catalog.model.Product item : ProductRepository.getProducts()) {
            if (item.id != id) continue;
            ProductOptionsSheet.show(this, item, null, (options, quantity) -> {
                CartRepository.add(this, item, options, quantity);
                updateCartBadge();
                String size = options.size == null ? getString(R.string.options_no_size) : options.size.label;
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.options_added_snackbar, item.name, size, CartRepository.totalQuantity(this)), Snackbar.LENGTH_SHORT).show();
            });
            return;
        }
    }
    private void toggleFavorite(DrinkProduct product) {
        int numId = parseNumericId(product.id);
        boolean isFavorite = FavoriteRepository.toggleFavorite(this, numId);
        favorites.put(product.id, isFavorite);
        homePreferences.edit().putBoolean("favorite_" + product.id, isFavorite).apply();
        Toast.makeText(this, getString(isFavorite ? R.string.home_favorite_added : R.string.home_favorite_removed, product.name), Toast.LENGTH_SHORT).show();
        updateProducts(currentQuery());
    }

    private int parseNumericId(String stringId) {
        if (stringId == null) return 0;
        switch (stringId) {
            case "nau_da": return 1;
            case "bac_xiu": return 2;
            case "cafe_phin": return 3;
            case "tra_xoai_chanh_day": return 4;
            case "tra_sen_vang": return 5;
            case "tra_coc_xi_muoi": return 6;
            case "tra_dao_cam_xa": case "peach_tea": return 7;
            case "tra_dua_luoi": return 8;
            case "tra_thach_dao": return 9;
            case "tra_vai": case "lychee_tea": return 10;
            case "cacao_da_xay": case "cocoa_ice": return 11;
            case "matcha_latte": return 12;
            case "cheese_cake": return 13;
            case "croissant": return 14;
            case "tiramisu": return 15;
            default:
                try { return Integer.parseInt(stringId); } catch (Exception e) { return 0; }
        }
    }

    private DrinkProduct findProduct(String id) {
        for (DrinkProduct product : products) if (product.id.equals(id)) return product;
        return null;
    }

    private DrinkProduct findDrinkProduct(String id) {
        return findProduct(id);
    }

    private String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('\u0111', 'd').trim();
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
