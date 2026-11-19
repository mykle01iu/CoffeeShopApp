package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.catalog.ui.ProductAdapter;
import com.coffeeshop.client.catalog.ui.ProductDetailActivity;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.data.FavoriteRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class FavoritesActivity extends Activity {

    private RecyclerView recyclerView;
    private View emptyView;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView title = findViewById(R.id.favoritesTitle);
        if (title != null) {
            title.setTypeface(FontUtils.lora(this, 600));
        }

        View backBtn = findViewById(R.id.favoritesBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyView = findViewById(R.id.favoritesEmptyView);

        View exploreBtn = findViewById(R.id.btnExploreMenu);
        if (exploreBtn != null) {
            exploreBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, MenuActivity.class));
                finish();
            });
        }

        adapter = new ProductAdapter(new ProductAdapter.OnClick() {
            @Override
            public void onClick(Product product) {
                Intent intent = new Intent(FavoritesActivity.this, ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id);
                startActivity(intent);
            }

            @Override
            public boolean supportsAdd() {
                return true;
            }

            @Override
            public void onAdd(Product product, View anchor) {
                ProductOptionsSheet.show(FavoritesActivity.this, product, null, (options, quantity) -> {
                    CartRepository.add(FavoritesActivity.this, product, options, quantity);
                    String size = options.size == null ? getString(R.string.options_no_size) : options.size.label;
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.options_added_snackbar, product.name, size, CartRepository.totalQuantity(FavoritesActivity.this)),
                            Snackbar.LENGTH_SHORT).show();
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<Product> favoriteProducts = FavoriteRepository.getFavoriteProducts(this);
        if (favoriteProducts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.submit(favoriteProducts);
        }
    }
}
