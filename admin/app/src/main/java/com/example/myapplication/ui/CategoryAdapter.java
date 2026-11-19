package com.example.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    public interface Listener {
        void onOpen(Category category);
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private final List<Category> categories = new ArrayList<>();
    private final Listener listener;
    private Map<String, Integer> productCounts = new HashMap<>();

    public CategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Category> items) {
        categories.clear();
        categories.addAll(items);
        notifyDataSetChanged();
    }

    public void setProductCounts(Map<String, Integer> counts) {
        productCounts = new HashMap<>(counts);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.name.setText(category.name);
        holder.description.setText(category.description == null || category.description.isEmpty()
                ? "Chưa có mô tả" : category.description);
        Integer count = productCounts.get(category.code.toUpperCase(java.util.Locale.ROOT));
        holder.productCount.setText(count == null
                ? "Đang tải số sản phẩm"
                : count + " sản phẩm đang kinh doanh");
        holder.edit.setOnClickListener(v -> listener.onEdit(category));
        holder.delete.setOnClickListener(v -> listener.onDelete(category));
        holder.itemView.setOnClickListener(v -> listener.onOpen(category));
    }

    @Override public int getItemCount() { return categories.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView name, description, productCount, edit, delete;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.category_name);
            description = itemView.findViewById(R.id.category_description);
            productCount = itemView.findViewById(R.id.category_product_count);
            edit = itemView.findViewById(R.id.edit_category);
            delete = itemView.findViewById(R.id.delete_category);
        }
    }
}
