package com.coffeeshop.client.ui.home.adapter;

import android.graphics.Color;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    public interface OnProductActionListener {
        boolean isFavorite(Product product);
        void onProductClick(Product product);
        void onFavoriteClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductActionListener listener;
    private final int cardWidthPx;
    private final int spacingPx;

    public ProductAdapter(List<Product> initialProducts, int cardWidthPx, int spacingPx,
                          OnProductActionListener listener) {
        this.cardWidthPx = cardWidthPx;
        this.spacingPx = spacingPx;
        this.listener = listener;
        updateData(initialProducts);
    }

    public void updateData(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink, parent, false);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(cardWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, spacingPx, 0);
        view.setLayoutParams(params);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.image.setImageResource(product.getImageRes());
        holder.image.setContentDescription(product.getName());
        holder.rating.setText(holder.itemView.getContext().getString(
                R.string.home_rating_format, product.getRating()));
        holder.name.setText(product.getName());
        holder.name.setTypeface(FontUtils.inter(holder.itemView.getContext(), 600));
        holder.description.setText(product.getDescription());
        holder.price.setText(holder.itemView.getContext().getString(R.string.product_price_format,NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(product.getPrice())));
        holder.price.setTypeface(FontUtils.inter(holder.itemView.getContext(), 700));

        boolean favorite = listener != null && listener.isFavorite(product);
        if (favorite) {
            holder.favorite.setImageResource(R.drawable.ic_favorite_filled);
            holder.favorite.setColorFilter(Color.parseColor("#E53935"));
        } else {
            holder.favorite.setImageResource(R.drawable.ic_favorite_border);
            holder.favorite.setColorFilter(Color.parseColor("#7D6C61"));
        }
        holder.favorite.setContentDescription(holder.itemView.getContext().getString(
                favorite ? R.string.home_favorite_removed : R.string.home_favorite_added, product.getName()));
        holder.favorite.setOnClickListener(view -> { if (listener != null) listener.onFavoriteClick(product); });
        holder.add.setContentDescription(holder.itemView.getContext().getString(R.string.home_add_product, product.getName()));
        holder.add.setOnClickListener(view -> { if (listener != null) listener.onProductClick(product); });
        holder.itemView.setOnClickListener(view -> { if (listener != null) listener.onProductClick(product); });
    }

    @Override public int getItemCount() { return products.size(); }

    static final class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView rating;
        final TextView name;
        final TextView description;
        final TextView price;
        final ImageButton favorite;
        final TextView add;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.drinkImage);
            rating = itemView.findViewById(R.id.drinkRating);
            name = itemView.findViewById(R.id.drinkName);
            description = itemView.findViewById(R.id.drinkDescription);
            price = itemView.findViewById(R.id.drinkPrice);
            favorite = itemView.findViewById(R.id.favoriteDrinkButton);
            add = itemView.findViewById(R.id.addDrinkButton);
        }
    }
}
