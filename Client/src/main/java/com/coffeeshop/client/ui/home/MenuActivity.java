package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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

import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.data.auth.TokenManager;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.CategoryDto;
import com.coffeeshop.client.data.remote.dto.ProductDto;
import com.coffeeshop.client.model.SelectedOptions;
import com.coffeeshop.client.ui.auth.LoginActivity;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.snackbar.Snackbar;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {
    private final List<CategoryDto> categories = new ArrayList<>();
    private final List<ProductDto> products = new ArrayList<>();
    private MenuViewModel viewModel;
    private LinearLayout productRows;
    private LinearLayout categoryContainer;
    private View loadingState;
    private View errorState;
    private TextView errorMessage;
    private TextView resultCount;
    private TextView emptyState;
    private TextView selectedChip;
    private String selectedCategoryCode;
    private String categoryError;
    private boolean categoryLoadFailed;
    private String query = "";
    private int productRequestState = ApiResult.Status.LOADING.ordinal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        TextView title = findViewById(R.id.menuTitle);
        title.setFocusable(true);
        title.setTypeface(FontUtils.lora(this, 500));

        categoryContainer = findViewById(R.id.menuCategoryContainer);
        productRows = findViewById(R.id.menuProductRows);
        resultCount = findViewById(R.id.menuResultCount);
        emptyState = findViewById(R.id.menuEmptyState);
        loadingState = findViewById(R.id.menuLoadingState);
        errorState = findViewById(R.id.menuErrorState);
        errorMessage = findViewById(R.id.menuErrorMessage);
        productRows.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        findViewById(R.id.menuLogoutButton).setOnClickListener(view -> confirmLogout());
        findViewById(R.id.menuCartButton).setOnClickListener(view ->
                startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.menuRetryButton).setOnClickListener(view -> retryLoading());
        setupSearch();
        setupNavigation();

        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        viewModel.getCategories().observe(this, this::onCategoriesResult);
        viewModel.getProducts().observe(this, this::onProductsResult);
        retryLoading();
    }

    private void retryLoading() {
        categoryError = null;
        categoryLoadFailed = false;
        errorState.setVisibility(View.GONE);
        viewModel.loadCategories();
        viewModel.loadProducts(selectedCategoryCode);
    }

    private void onCategoriesResult(ApiResult<List<CategoryDto>> result) {
        if (result == null || result.status == ApiResult.Status.LOADING) return;
        if (result.status == ApiResult.Status.ERROR) {
            categoryLoadFailed = true;
            categoryError = result.message;
            categories.clear();
            createCategories();
            showError(result.message);
            return;
        }
        categoryLoadFailed = false;
        categoryError = null;
        categories.clear();
        if (result.data != null) {
            for (CategoryDto category : result.data) {
                if (category != null && !Boolean.FALSE.equals(category.available)) categories.add(category);
            }
        }
        createCategories();
    }

    private void onProductsResult(ApiResult<List<ProductDto>> result) {
        if (result == null) return;
        if (result.status == ApiResult.Status.LOADING) {
            productRequestState = ApiResult.Status.LOADING.ordinal();
            loadingState.setVisibility(View.VISIBLE);
            errorState.setVisibility(View.GONE);
            productRows.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            return;
        }
        loadingState.setVisibility(View.GONE);
        if (result.status == ApiResult.Status.ERROR) {
            productRequestState = ApiResult.Status.ERROR.ordinal();
            showError(result.message);
            return;
        }
        productRequestState = ApiResult.Status.SUCCESS.ordinal();
        if (categoryLoadFailed) showCategoryWarning();
        else errorState.setVisibility(View.GONE);
        products.clear();
        if (result.data != null) {
            for (ProductDto product : result.data) {
                if (product != null && !Boolean.FALSE.equals(product.available)) products.add(product);
            }
        }
        renderProducts();
    }

    private void showError(String message) {
        loadingState.setVisibility(View.GONE);
        productRows.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorMessage.setText(message == null || message.trim().isEmpty()
                ? "Không tải được thực đơn. Vui lòng thử lại." : message);
        errorState.setVisibility(View.VISIBLE);
    }

    private void showCategoryWarning() {
        errorMessage.setText(categoryError == null || categoryError.trim().isEmpty()
                ? "Không tải được danh mục. Vui lòng thử lại." : categoryError);
        errorState.setVisibility(View.VISIBLE);
    }

    private void createCategories() {
        categoryContainer.removeAllViews();
        addCategoryChip(getString(R.string.category_all), null);
        for (CategoryDto category : categories) {
            if (category.code != null && !category.code.trim().isEmpty()) {
                addCategoryChip(category.name == null ? category.code : category.name, category.code);
            }
        }
        updateChipStyles();
    }

    private void addCategoryChip(String label, String code) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextSize(13);
        chip.setTypeface(FontUtils.inter(this, 600));
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(16), 0, dp(16), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        params.setMarginEnd(dp(8));
        categoryContainer.addView(chip, params);
        if ((selectedCategoryCode == null && code == null)
                || (selectedCategoryCode != null && selectedCategoryCode.equals(code))) selectedChip = chip;
        chip.setOnClickListener(view -> {
            selectedCategoryCode = code;
            selectedChip = chip;
            updateChipStyles();
            viewModel.loadProducts(code);
        });
    }

    private void updateChipStyles() {
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            TextView chip = (TextView) categoryContainer.getChildAt(i);
            boolean active = chip == selectedChip;
            chip.setBackgroundResource(active
                    ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
            chip.setTextColor(getColor(active ? R.color.coffee_surface : R.color.coffee_muted));
        }
    }

    private void setupSearch() {
        EditText input = findViewById(R.id.menuSearchInput);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString();
                if (productRequestState == ApiResult.Status.SUCCESS.ordinal()) renderProducts();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void renderProducts() {
        String normalized = normalize(query);
        List<ProductDto> visible = new ArrayList<>();
        for (ProductDto product : products) {
            if (normalized.isEmpty() || normalize(product.name).contains(normalized)
                    || normalize(product.description).contains(normalized)) visible.add(product);
        }
        productRows.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int screenWidthDp = Math.round(getResources().getDisplayMetrics().widthPixels / density);
        int cardWidth = dp((screenWidthDp - 60) / 2);
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

    private void bindCard(LinearLayout row, ProductDto product, int width) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_drink, row, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (row.getChildCount() == 0) params.setMarginEnd(dp(12));
        row.addView(card, params);

        ImageView image = card.findViewById(R.id.drinkImage);
        image.setContentDescription(product.name);

        int localRes = getProductImageResource(product.name);
        if (localRes != 0) {
            image.setImageResource(localRes);
        } else if (product.imageUrl != null && !product.imageUrl.trim().isEmpty()) {
            Glide.with(this).load(product.imageUrl)
                    .placeholder(R.drawable.logo_moc_mien_icon)
                    .error(R.drawable.logo_moc_mien_icon)
                    .into(image);
        } else {
            image.setImageResource(R.drawable.logo_moc_mien_icon);
        }

        ((TextView) card.findViewById(R.id.drinkRating)).setVisibility(View.GONE);
        TextView name = card.findViewById(R.id.drinkName);
        name.setText(product.name);
        name.setTypeface(FontUtils.inter(this, 600));
        ((TextView) card.findViewById(R.id.drinkDescription)).setText(product.description == null ? "" : product.description);
        TextView price = card.findViewById(R.id.drinkPrice);
        price.setText(NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(
                product.price == null ? 0 : Math.round(product.price)) + " đ");
        price.setTypeface(FontUtils.inter(this, 700));
        ImageButton favorite = card.findViewById(R.id.favoriteDrinkButton);
        favorite.setVisibility(View.GONE);
        TextView add = card.findViewById(R.id.addDrinkButton);
        add.setContentDescription(getString(R.string.home_add_product,
                product.name == null ? "sản phẩm" : product.name));
        add.setOnClickListener(view -> addToCart(product));
        card.setOnClickListener(view -> addToCart(product));
        image.setOnClickListener(view -> addToCart(product));
    }

    private void addToCart(ProductDto remote) {
        if (remote == null) return;
        Product catalogProduct = toCatalogProduct(remote);
        if (catalogProduct == null) {
            Toast.makeText(this, "Không thể chọn món này.", Toast.LENGTH_SHORT).show();
            return;
        }
        ProductOptionsSheet.show(this, catalogProduct, null, (options, quantity) -> {
            CartRepository.add(this, catalogProduct, options, quantity);
            String size = options.size == null ? getString(R.string.options_no_size) : options.size.label;
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.options_added_snackbar, catalogProduct.name, size, CartRepository.totalQuantity(this)),
                    Snackbar.LENGTH_SHORT).show();
        });
    }

    private Product toCatalogProduct(ProductDto remote) {
        if (remote == null || remote.id == null) return null;
        int id = remote.id.intValue();
        int imageRes = getProductImageResource(remote.name);
        if (imageRes == 0) imageRes = R.drawable.logo_moc_mien_icon;
        int priceInt = remote.price == null ? 0 : (int) Math.round(remote.price);
        return new Product(
                id,
                remote.name == null ? "" : remote.name,
                remote.category == null ? "" : remote.category,
                priceInt,
                4.9f,
                20,
                remote.description == null ? "" : remote.description,
                imageRes,
                getProductImageResource(remote.name) == 0 ? remote.imageUrl : null
        );
    }

    private int getProductImageResource(String productName) {
        String name = normalize(productName);
        if (name.contains("nau da")) return R.drawable.nauda;
        if (name.contains("sua da") || name.contains("ca phe sua")) return R.drawable.nauda;
        if (name.contains("bac xiu")) return R.drawable.bacxiu;
        if (name.contains("phin") || name.contains("americano")) return R.drawable.caphephin;
        if (name.contains("xoai")) return R.drawable.traxoaichanhday1;
        if (name.contains("sen")) return R.drawable.trasenvang;
        if (name.contains("xi muoi") || name.contains("coc")) return R.drawable.tracocximuoi;
        if (name.contains("sa")) return R.drawable.tradaocamxa;
        if (name.contains("dua luoi")) return R.drawable.tradualuoi;
        if (name.contains("thach dao")) return R.drawable.trathachdao;
        if (name.contains("vai")) return R.drawable.travai;
        if (name.contains("cacao")) return R.drawable.cacaodaxay;
        if (name.contains("matcha")) return R.drawable.matchalatte;
        if (name.contains("cheese")) return R.drawable.cheesecake;
        if (name.contains("croissant")) return R.drawable.croissant;
        if (name.contains("tiramisu")) return R.drawable.timarisu;
        return 0;
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_button)
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.logout_button, (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        TokenManager.getInstance(this).clear();
        Intent intent = new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        findViewById(R.id.navHomeItem).setOnClickListener(view -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navMenuItem).setOnClickListener(view ->
                ((ScrollView) findViewById(R.id.menuScrollView)).smoothScrollTo(0, 0));
        findViewById(R.id.navOrdersItem).setOnClickListener(view ->
                startActivity(new Intent(this, OrdersActivity.class)));
        findViewById(R.id.navProfileItem).setOnClickListener(view ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd').trim();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
