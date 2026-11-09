package com.example.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    interface Listener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private final ArrayList<Product> products = new ArrayList<>();
    private final Listener listener;

    ProductAdapter(Listener listener) { this.listener = listener; }

    void submitList(List<Product> items) {
        products.clear();
        products.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.name.setText(product.name);
        holder.price.setText(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(product.price) + " đ");
        holder.description.setText(product.description.isEmpty() ? "Chưa có mô tả" : product.description);
        ProductImageLoader.load(holder.image, product.imageUri);
        holder.edit.setOnClickListener(v -> listener.onEdit(product));
        holder.delete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name, price, description, edit, delete;
        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            description = itemView.findViewById(R.id.product_description);
            edit = itemView.findViewById(R.id.edit_product);
            delete = itemView.findViewById(R.id.delete_product);
        }
    }
}
