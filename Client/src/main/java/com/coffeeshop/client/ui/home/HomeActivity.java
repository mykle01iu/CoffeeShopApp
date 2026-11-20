package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.data.CartManager;
import com.coffeeshop.client.data.CartRepository;
import com.google.android.material.snackbar.Snackbar;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.data.FavoriteRepository;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.CategoryDto;
import com.coffeeshop.client.data.remote.dto.ProductDto;
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

public class HomeActivity extends androidx.appcompat.app.AppCompatActivity {
    private final List<Product> homeProducts = new ArrayList<>();
    private final List<Category> homeCategories = new ArrayList<>();
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
    private MenuViewModel menuViewModel;

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

        RecyclerView categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        categoryRecyclerView.setHasFixedSize(true);
        homeCategories.add(new Category("all", getString(R.string.category_all), R.drawable.ic_menu));
        categoryAdapter = new CategoryAdapter(this, homeCategories, this::selectCategory);
        categoryRecyclerView.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(homeProducts, dp(200), dp(14), new ProductAdapter.OnProductActionListener() {
            @Override
            public boolean isFavorite(Product product) {
                return isProductFavorite(product);
            }

            @Override
            public void onProductClick(Product product) {
                addProduct(product);
            }

            @Override
            public void onFavoriteClick(Product product) {
                toggleFavorite(product);
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

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuViewModel.getCategories().observe(this, this::onCategoriesResult);
        menuViewModel.getProducts().observe(this, this::onProductsResult);
        menuViewModel.loadCategories();
        menuViewModel.loadProducts(null);
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
        if (productAdapter != null) updateProducts(currentQuery());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("home.selectedCategory", selectedCategory);
        super.onSaveInstanceState(outState);
    }

    private void onCategoriesResult(ApiResult<List<CategoryDto>> result) {
        if (result == null || result.status != ApiResult.Status.SUCCESS) return;
        homeCategories.clear();
        homeCategories.add(new Category("all", getString(R.string.category_all), R.drawable.ic_menu));
        if (result.data != null) for (CategoryDto item : result.data) {
            if (item == null || Boolean.FALSE.equals(item.available) || item.code == null) continue;
            homeCategories.add(new Category(item.code, item.name == null ? item.code : item.name, categoryIcon(item.code)));
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private void onProductsResult(ApiResult<List<ProductDto>> result) {
        if (result == null || result.status == ApiResult.Status.LOADING) return;
        if (result.status == ApiResult.Status.ERROR) {
            homeProducts.clear();
            updateProducts(currentQuery());
            Toast.makeText(this, "Không tải được sản phẩm. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }
        homeProducts.clear();
        if (result.data != null) for (ProductDto dto : result.data) {
            if (dto == null || Boolean.FALSE.equals(dto.available) || dto.id == null) continue;
            homeProducts.add(toHomeProduct(dto));
        }
        updateProducts(currentQuery());
    }

    private Product toHomeProduct(ProductDto dto) {
        String id = String.valueOf(dto.id);
        return new Product(id, categoryCode(dto.category), dto.name == null ? "" : dto.name,
                dto.description == null ? "" : dto.description,
                dto.price == null ? 0 : dto.price, 0f, imageForProduct(dto.id), dto.imageUrl);
    }

    private String categoryCode(String category) {
        if (category == null) return "";
        String value = normalize(category);
        if (value.contains("ca phe") || value.equals("coffee")) return "coffee";
        if (value.contains("tra") || value.equals("tea")) return "tea";
        if (value.contains("banh") || value.equals("snack")) return "snack";
        if (value.contains("da xay") || value.equals("smoothie")) return "smoothie";
        if (value.contains("khac") || value.equals("latte")) return "other";
        return value.replace(' ', '_');
    }

    private int categoryIcon(String code) {
        switch (categoryCode(code)) {
            case "coffee": return R.drawable.ic_splash_cup;
            case "tea": return R.drawable.ic_splash_cup;
            case "snack": return R.drawable.img_snack_croissant;
            case "smoothie": return R.drawable.img_mango_smoothie;
            default: return R.drawable.ic_menu;
        }
    }

    private int imageForProduct(Long id) {
        if (id == null) return R.drawable.ic_menu;
        switch (id.intValue()) {
            case 1: return R.drawable.nauda; case 2: return R.drawable.bacxiu;
            case 3: return R.drawable.caphephin; case 4: return R.drawable.traxoaichanhday1;
            case 5: return R.drawable.trasenvang; case 6: return R.drawable.tracocximuoi;
            case 7: return R.drawable.tradaocamxa; case 8: return R.drawable.tradualuoi;
            case 9: return R.drawable.trathachdao; case 10: return R.drawable.travai;
            case 11: return R.drawable.cacaodaxay; case 12: return R.drawable.matchalatte;
            case 13: return R.drawable.cheesecake; case 14: return R.drawable.croissant;
            case 15: return R.drawable.timarisu; default: return R.drawable.ic_menu;
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
        selectedCategory = categoryCode(category.getId());
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

    private void addProduct(Product product) {
        int id = parseNumericId(product.getId());
        if (id <= 0) return;
        com.coffeeshop.client.catalog.model.Product item = new com.coffeeshop.client.catalog.model.Product(
                id, product.getName(), product.getCategoryId(), (int) Math.round(product.getPrice()),
                product.getRating(), 0, product.getDescription(), product.getImageRes(), product.getImageUrl());
        ProductOptionsSheet.show(this, item, null, (options, quantity) -> {
            CartRepository.add(this, item, options, quantity);
            updateCartBadge();
            String size = options.size == null ? getString(R.string.options_no_size) : options.size.label;
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.options_added_snackbar, item.name, size, CartRepository.totalQuantity(this)), Snackbar.LENGTH_SHORT).show();
        });
    }
    private void toggleFavorite(Product product) {
        int numId = parseNumericId(product.getId());
        boolean isFavorite = FavoriteRepository.toggleFavorite(this, numId);
        favorites.put(product.getId(), isFavorite);
        homePreferences.edit().putBoolean("favorite_" + product.getId(), isFavorite).apply();
        Toast.makeText(this, getString(isFavorite ? R.string.home_favorite_added : R.string.home_favorite_removed, product.getName()), Toast.LENGTH_SHORT).show();
        updateProducts(currentQuery());
    }

    private int parseNumericId(String stringId) {
        if (stringId == null) return 0;
        try { return Integer.parseInt(stringId); } catch (Exception e) { return 0; }
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
