package com.example.myapplication.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CategoryRepository;
import com.example.myapplication.data.ProductRepository;
import com.example.myapplication.model.Category;

public class CategoryScreen implements CategoryAdapter.Listener {
    private final Activity activity;
    private final Navigator navigator;
    private final CategoryRepository repository;
    private final ProductRepository products;
    private CategoryAdapter adapter;

    public CategoryScreen(Activity activity, Navigator navigator, CategoryRepository repository,
                          ProductRepository products) {
        this.activity = activity;
        this.navigator = navigator;
        this.repository = repository;
        this.products = products;
    }

    public void show() {
        activity.setContentView(R.layout.categories);
        activity.findViewById(R.id.back_categories).setOnClickListener(v -> navigator.showDashboard());
        activity.findViewById(R.id.add_category).setOnClickListener(v -> showCategoryForm(null));
        RecyclerView list = activity.findViewById(R.id.categories_list);
        list.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new CategoryAdapter(this, products);
        list.setAdapter(adapter);
        refreshList();
    }

    @Override public void onOpen(Category category) { navigator.showProducts(category); }

    @Override public void onEdit(Category category) { showCategoryForm(category); }

    @Override public void onDelete(Category category) {
        new AlertDialog.Builder(activity)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + category.name + "\" không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.delete(category);
                    refreshList();
                    Toast.makeText(activity, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void showCategoryForm(Category category) {
        boolean editing = category != null;
        View form = LayoutInflater.from(activity).inflate(R.layout.category_form, null, false);
        EditText nameInput = form.findViewById(R.id.input_category_name);
        EditText descriptionInput = form.findViewById(R.id.input_category_description);
        if (editing) {
            nameInput.setText(category.name);
            descriptionInput.setText(category.description);
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(editing ? "Sửa danh mục" : "Thêm danh mục")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu" : "Thêm", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            if (name.isEmpty()) { nameInput.setError("Vui lòng nhập tên danh mục"); return; }
            if (repository.containsName(name, category)) { nameInput.setError("Tên danh mục đã tồn tại"); return; }
            if (editing) repository.update(category, name, description);
            else repository.add(name, description);
            refreshList();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void refreshList() {
        if (adapter != null) adapter.submitList(repository.getAll());
        TextView count = activity.findViewById(R.id.category_count);
        if (count != null) count.setText(repository.getAll().size() + " danh mục");
        View empty = activity.findViewById(R.id.categories_empty);
        if (empty != null) empty.setVisibility(repository.getAll().isEmpty() ? View.VISIBLE : View.GONE);
    }
}
