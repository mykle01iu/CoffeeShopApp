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
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.data.BackendApi;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryScreen implements CategoryAdapter.Listener {
    private final Activity activity;
    private final Navigator navigator;
    private final BackendApi api;
    private final SessionManager session;
    private CategoryAdapter adapter;
    private List<Category> categories = java.util.Collections.emptyList();

    public CategoryScreen(Activity activity, Navigator navigator, BackendApi api,
                          SessionManager session) {
        this.activity = activity;
        this.navigator = navigator;
        this.api = api;
        this.session = session;
    }

    public void show() {
        activity.setContentView(R.layout.categories);
        activity.findViewById(R.id.back_categories).setOnClickListener(v -> navigator.showDashboard());
        activity.findViewById(R.id.add_category).setOnClickListener(v -> showCategoryForm(null));
        RecyclerView list = activity.findViewById(R.id.categories_list);
        list.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new CategoryAdapter(this);
        list.setAdapter(adapter);
        setLoading();
        refreshData();
    }

    @Override public void onOpen(Category category) { navigator.showProducts(category); }

    @Override public void onEdit(Category category) { showCategoryForm(category); }

    @Override public void onDelete(Category category) {
        api.getProducts(new BackendApi.Callback<List<Product>>() {
            @Override public void onSuccess(List<Product> products) {
                if (!isVisible()) return;
                int count = 0;
                for (Product product : products) {
                    if (category.code.equalsIgnoreCase(product.categoryCode)) count++;
                }
                if (count > 0) {
                    Toast.makeText(activity,
                            "Hãy xóa " + count + " sản phẩm thuộc danh mục này trước.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                confirmDelete(category);
            }

            @Override public void onError(BackendApi.ApiError error) { handleError(error); }
        });
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(activity)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + category.name + "\" không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> api.deleteCategory(category.id,
                        new BackendApi.Callback<Void>() {
                            @Override public void onSuccess(Void ignored) {
                                Toast.makeText(activity, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                                refreshData();
                            }
                            @Override public void onError(BackendApi.ApiError error) { handleError(error); }
                        })).show();
    }

    private void showCategoryForm(Category category) {
        boolean editing = category != null;
        View form = LayoutInflater.from(activity).inflate(R.layout.category_form, null, false);
        EditText codeInput = form.findViewById(R.id.input_category_code);
        EditText nameInput = form.findViewById(R.id.input_category_name);
        EditText descriptionInput = form.findViewById(R.id.input_category_description);
        if (editing) {
            codeInput.setText(category.code);
            codeInput.setEnabled(false); // Product.category refers to this stable code.
            nameInput.setText(category.name);
            descriptionInput.setText(category.description);
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(editing ? "Sửa danh mục" : "Thêm danh mục")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu" : "Thêm", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    String code = codeInput.getText().toString().trim().toUpperCase(Locale.ROOT);
                    String name = nameInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    if (code.isEmpty()) { codeInput.setError("Vui lòng nhập mã danh mục"); return; }
                    if (code.length() > 50) { codeInput.setError("Mã danh mục tối đa 50 ký tự"); return; }
                    if (name.isEmpty()) { nameInput.setError("Vui lòng nhập tên danh mục"); return; }
                    for (Category existing : categories) {
                        if (existing.id != (editing ? category.id : -1)
                                && existing.code.equalsIgnoreCase(code)) {
                            codeInput.setError("Mã danh mục đã tồn tại");
                            return;
                        }
                    }

                    View positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positive.setEnabled(false);
                    BackendApi.Callback<Category> callback = new BackendApi.Callback<Category>() {
                        @Override public void onSuccess(Category saved) {
                            if (!isVisible()) return;
                            Toast.makeText(activity, editing ? "Đã cập nhật danh mục" : "Đã thêm danh mục",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            refreshData();
                        }

                        @Override public void onError(BackendApi.ApiError error) {
                            positive.setEnabled(true);
                            if (error.statusCode == 401 || error.statusCode == 403) {
                                dialog.dismiss();
                                handleError(error);
                            } else if (error.statusCode == 409) {
                                codeInput.setError(error.message);
                            } else {
                                Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    if (editing) api.updateCategory(category, code, name, description, callback);
                    else api.createCategory(code, name, description, callback);
                }));
        dialog.show();
    }

    private void refreshData() {
        api.getCategories(new BackendApi.Callback<List<Category>>() {
            @Override public void onSuccess(List<Category> result) {
                if (!isVisible()) return;
                categories = result;
                adapter.submitList(categories);
                TextView count = activity.findViewById(R.id.category_count);
                if (count != null) count.setText(categories.size() + " danh mục");
                TextView empty = activity.findViewById(R.id.categories_empty);
                if (empty != null) {
                    empty.setText("Chưa có danh mục nào.\nBấm 'Thêm danh mục' để bắt đầu.");
                    empty.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override public void onError(BackendApi.ApiError error) {
                if (!isVisible()) return;
                TextView empty = activity.findViewById(R.id.categories_empty);
                if (empty != null) {
                    empty.setText("Không tải được danh mục. Kéo ra/vào lại màn hình để thử lại.");
                    empty.setVisibility(View.VISIBLE);
                }
                handleError(error);
            }
        });

        api.getProducts(new BackendApi.Callback<List<Product>>() {
            @Override public void onSuccess(List<Product> products) {
                if (!isVisible()) return;
                Map<String, Integer> counts = new HashMap<>();
                for (Product product : products) {
                    String key = product.categoryCode.toUpperCase(Locale.ROOT);
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                }
                adapter.setProductCounts(counts);
            }

            @Override public void onError(BackendApi.ApiError error) {
                if (isVisible()) handleError(error);
            }
        });
    }

    private void setLoading() {
        TextView empty = activity.findViewById(R.id.categories_empty);
        if (empty != null) {
            empty.setText("Đang tải danh mục...");
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void handleError(BackendApi.ApiError error) {
        if (error.statusCode == 401 || error.statusCode == 403) {
            Toast.makeText(activity, error.message + " Đăng nhập lại để tiếp tục.", Toast.LENGTH_LONG).show();
            session.clear();
            navigator.showLogin();
            return;
        }
        Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show();
    }

    private boolean isVisible() {
        return activity.findViewById(R.id.categories_list) != null;
    }
}
