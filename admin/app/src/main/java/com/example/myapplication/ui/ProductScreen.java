package com.example.myapplication.ui;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductScreen implements ProductAdapter.Listener {
    private final Activity activity;
    private final Navigator navigator;
    private final BackendApi api;
    private final SessionManager session;
    private final Category category;
    private ProductAdapter adapter;
    private List<Product> categoryProducts = new ArrayList<>();

    public ProductScreen(Activity activity, Navigator navigator, BackendApi api,
                         SessionManager session, Category category) {
        this.activity = activity;
        this.navigator = navigator;
        this.api = api;
        this.session = session;
        this.category = category;
    }

    public void show() {
        activity.setContentView(R.layout.products);
        activity.findViewById(R.id.back_products).setOnClickListener(v -> navigator.showCategories());
        ((TextView) activity.findViewById(R.id.products_category_title)).setText(category.name);
        activity.findViewById(R.id.add_product).setOnClickListener(v -> showProductForm(null));
        RecyclerView list = activity.findViewById(R.id.products_list);
        list.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new ProductAdapter(this);
        list.setAdapter(adapter);
        setLoading();
        refreshList();
    }

    @Override public void onEdit(Product product) { showProductForm(product); }

    @Override public void onDelete(Product product) {
        new AlertDialog.Builder(activity)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa \"" + product.name + "\" khỏi danh mục " + category.name + " không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> api.deleteProduct(product.id,
                        new BackendApi.Callback<Void>() {
                            @Override public void onSuccess(Void ignored) {
                                Toast.makeText(activity, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                refreshList();
                            }
                            @Override public void onError(BackendApi.ApiError error) { handleError(error); }
                        })).show();
    }

    private void showProductForm(Product product) {
        boolean editing = product != null;
        View form = LayoutInflater.from(activity).inflate(R.layout.product_form, null, false);
        EditText nameInput = form.findViewById(R.id.input_product_name);
        EditText priceInput = form.findViewById(R.id.input_product_price);
        EditText descriptionInput = form.findViewById(R.id.input_product_description);
        EditText imageUrlInput = form.findViewById(R.id.input_product_image_url);
        ImageView preview = form.findViewById(R.id.product_image_preview);
        TextView previewButton = form.findViewById(R.id.preview_product_image);

        String initialImage = editing ? product.imageUri : "";
        imageUrlInput.setText(initialImage == null ? "" : initialImage);
        ProductImageLoader.load(preview, initialImage);
        previewButton.setOnClickListener(v -> ProductImageLoader.load(preview,
                imageUrlInput.getText().toString().trim()));
        if (editing) {
            nameInput.setText(product.name);
            priceInput.setText(BigDecimal.valueOf(product.price).stripTrailingZeros().toPlainString());
            descriptionInput.setText(product.description);
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(editing ? "Sửa sản phẩm" : "Thêm sản phẩm")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu" : "Thêm", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    String name = nameInput.getText().toString().trim();
                    String priceText = priceInput.getText().toString().trim().replace(',', '.');
                    String description = descriptionInput.getText().toString().trim();
                    String imageUrl = imageUrlInput.getText().toString().trim();

                    if (name.isEmpty()) { nameInput.setError("Vui lòng nhập tên sản phẩm"); return; }
                    if (name.length() > 255) { nameInput.setError("Tên sản phẩm tối đa 255 ký tự"); return; }
                    double price;
                    try { price = Double.parseDouble(priceText); }
                    catch (NumberFormatException exception) {
                        priceInput.setError("Vui lòng nhập giá hợp lệ");
                        return;
                    }
                    if (!Double.isFinite(price) || price <= 0) {
                        priceInput.setError("Giá phải lớn hơn 0");
                        return;
                    }
                    if (imageUrl.length() > 255 || !isHttpUrlOrEmpty(imageUrl)) {
                        imageUrlInput.setError("Ảnh phải là URL HTTP/HTTPS tối đa 255 ký tự");
                        return;
                    }
                    for (Product existing : categoryProducts) {
                        if (existing.id != (editing ? product.id : -1)
                                && existing.name.equalsIgnoreCase(name)) {
                            nameInput.setError("Tên sản phẩm đã tồn tại trong danh mục này");
                            return;
                        }
                    }

                    View positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positive.setEnabled(false);
                    BackendApi.Callback<Product> callback = new BackendApi.Callback<Product>() {
                        @Override public void onSuccess(Product saved) {
                            if (!isVisible()) return;
                            Toast.makeText(activity, editing ? "Đã cập nhật sản phẩm" : "Đã thêm sản phẩm",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            refreshList();
                        }

                        @Override public void onError(BackendApi.ApiError error) {
                            positive.setEnabled(true);
                            if (error.statusCode == 401 || error.statusCode == 403) {
                                dialog.dismiss();
                                handleError(error);
                            } else {
                                Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    if (editing) api.updateProduct(product, category.code, name, price, description,
                            imageUrl, callback);
                    else api.createProduct(category.code, name, price, description, imageUrl, callback);
                }));
        dialog.show();
    }

    private boolean isHttpUrlOrEmpty(String value) {
        if (value == null || value.isEmpty()) return true;
        try {
            Uri uri = Uri.parse(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void refreshList() {
        TextView empty = activity.findViewById(R.id.products_empty);
        if (empty != null) {
            empty.setText("Đang tải sản phẩm...");
            empty.setVisibility(View.VISIBLE);
        }
        api.getProducts(new BackendApi.Callback<List<Product>>() {
            @Override public void onSuccess(List<Product> allProducts) {
                if (!isVisible()) return;
                categoryProducts = new ArrayList<>();
                for (Product product : allProducts) {
                    if (category.code.equalsIgnoreCase(product.categoryCode)) categoryProducts.add(product);
                }
                adapter.submitList(categoryProducts);
                TextView count = activity.findViewById(R.id.product_count);
                if (count != null) count.setText(categoryProducts.size() + " sản phẩm");
                TextView emptyView = activity.findViewById(R.id.products_empty);
                if (emptyView != null) {
                    emptyView.setText("Danh mục chưa có sản phẩm.\nNhấn Thêm sản phẩm để bắt đầu.");
                    emptyView.setVisibility(categoryProducts.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override public void onError(BackendApi.ApiError error) {
                if (!isVisible()) return;
                TextView emptyView = activity.findViewById(R.id.products_empty);
                if (emptyView != null) {
                    emptyView.setText("Không tải được sản phẩm. Quay lại danh mục rồi mở lại để thử.");
                    emptyView.setVisibility(View.VISIBLE);
                }
                handleError(error);
            }
        });
    }

    private void setLoading() {
        TextView empty = activity.findViewById(R.id.products_empty);
        if (empty != null) {
            empty.setText("Đang tải sản phẩm...");
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
        return activity.findViewById(R.id.products_list) != null;
    }
}
