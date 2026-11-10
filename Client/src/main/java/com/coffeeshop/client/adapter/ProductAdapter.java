package com.coffeeshop.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.model.Product;
import com.coffeeshop.client.ui.common.FontUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onFavoriteClick(Product product);
        void onAddToCartClick(Product product);
    }

    private final Context context;
    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productsList, OnProductClickListener listener) {
        this.context = context;
        if (productsList != null) {
            this.products.addAll(productsList);
        }
        this.listener = listener;
    }

    public void updateData(List<Product> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drink, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.drinkImage.setImageResource(product.getImageRes());
        holder.drinkImage.setContentDescription(product.getName());
        holder.drinkRating.setText(context.getString(R.string.home_rating_format, product.getRating()));

        holder.drinkName.setText(product.getName());
        holder.drinkName.setTypeface(FontUtils.inter(context, 600));

        holder.drinkDescription.setText(product.getDescription());

        holder.drinkPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getPrice()));
        holder.drinkPrice.setTypeface(FontUtils.inter(context, 700));

        holder.favoriteButton.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(product);
        });

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(product);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView drinkImage;
        final TextView drinkRating;
        final TextView drinkName;
        final TextView drinkDescription;
        final TextView drinkPrice;
        final ImageButton favoriteButton;
        final TextView addButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImage);
            drinkRating = itemView.findViewById(R.id.drinkRating);
            drinkName = itemView.findViewById(R.id.drinkName);
            drinkDescription = itemView.findViewById(R.id.drinkDescription);
            drinkPrice = itemView.findViewById(R.id.drinkPrice);
            favoriteButton = itemView.findViewById(R.id.favoriteDrinkButton);
            addButton = itemView.findViewById(R.id.addDrinkButton);
        }
    }
}
