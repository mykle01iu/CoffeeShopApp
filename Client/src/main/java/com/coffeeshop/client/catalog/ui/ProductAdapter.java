package com.coffeeshop.client.catalog.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.data.FavoriteRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {
    public interface OnClick { void onClick(Product product); default void onAdd(Product product,View anchor) { } default boolean supportsAdd(){return false;} }
    private final List<Product> items = new ArrayList<>();
    private final OnClick onClick;

    public ProductAdapter(OnClick onClick) { this.onClick = onClick; }

    public void submit(List<Product> products) {
        items.clear();
        if (products != null) items.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalog_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Product p = items.get(position);
        Context ctx = h.itemView.getContext();

        h.name.setText(p.name);
        h.category.setText(p.category);
        h.price.setText(ctx.getString(R.string.product_price_format, NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(p.price)));
        h.rating.setText(ctx.getString(R.string.product_rating_format, stars(p.rating), p.rating, p.reviewCount));
        h.image.setImageResource(p.imageRes);

        boolean isFav = FavoriteRepository.isFavorite(ctx, p.id);
        updateFavoriteIcon(h.favoriteBtn, isFav);

        h.favoriteBtn.setOnClickListener(v -> {
            boolean newFav = FavoriteRepository.toggleFavorite(ctx, p.id);
            updateFavoriteIcon(h.favoriteBtn, newFav);
            Toast.makeText(ctx, newFav ? "Đã lưu " + p.name + " vào món yêu thích" : "Đã bỏ " + p.name + " khỏi món yêu thích", Toast.LENGTH_SHORT).show();
        });
        h.addBtn.setVisibility(onClick.supportsAdd()?View.VISIBLE:View.GONE);
        h.addBtn.setOnClickListener(v->onClick.onAdd(p,v));

        h.itemView.setOnClickListener(v -> onClick.onClick(p));
    }

    private void updateFavoriteIcon(ImageButton btn, boolean isFavorite) {
        if (isFavorite) {
            btn.setImageResource(R.drawable.ic_favorite_filled);
            btn.setColorFilter(Color.parseColor("#E53935"));
        } else {
            btn.setImageResource(R.drawable.ic_favorite_border);
            btn.setColorFilter(Color.parseColor("#7D6C61"));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    private static String stars(float rating) {
        int full = Math.max(0, Math.min(5, Math.round(rating)));
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 5; i++) value.append(i < full ? '★' : '☆');
        return value.toString();
    }

    public static final class Holder extends RecyclerView.ViewHolder {
        final TextView name, category, price, rating;
        final ImageView image;
        final ImageButton favoriteBtn; final TextView addBtn;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.productName);
            category = v.findViewById(R.id.productCategory);
            price = v.findViewById(R.id.productPrice);
            rating = v.findViewById(R.id.productRating);
            image = v.findViewById(R.id.productImage);
            favoriteBtn = v.findViewById(R.id.productFavorite);
            addBtn=v.findViewById(R.id.productAdd);
        }
    }
}
