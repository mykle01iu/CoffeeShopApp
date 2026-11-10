package com.coffeeshop.client.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.model.Category;

import java.util.ArrayList;
import java.util.List;

public final class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private String selectedId;

    public CategoryAdapter(List<Category> categories, String selectedId, OnCategoryClickListener listener) {
        this.categories.addAll(categories);
        this.selectedId = selectedId;
        this.listener = listener;
    }

    public void setSelectedCategory(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean selected = category.getId().equals(selectedId);
        holder.name.setText(category.getName());
        holder.name.setBackgroundResource(selected ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
        holder.name.setTextColor(holder.name.getContext().getColor(selected ? R.color.coffee_surface : R.color.coffee_muted));
        holder.name.setTypeface(com.coffeeshop.client.ui.common.FontUtils.inter(holder.name.getContext(), selected ? 600 : 500));
        holder.name.setOnClickListener(view -> {
            if (listener != null) listener.onCategoryClick(category);
        });
    }

    @Override public int getItemCount() { return categories.size(); }

    static final class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.homeCategoryName);
        }
    }
}
