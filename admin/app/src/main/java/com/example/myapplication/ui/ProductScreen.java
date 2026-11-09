package com.example.myapplication.ui;

import android.app.Activity;
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
import com.example.myapplication.data.CategoryRepository;
import com.example.myapplication.data.ProductRepository;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductScreen implements ProductAdapter.Listener {
    private final Activity activity;
    private final Navigator navigator;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final Category category;
    private ProductAdapter adapter;

    public ProductScreen(Activity activity, Navigator navigator, CategoryRepository categories,
                         ProductRepository products, Category category) {
        this.activity = activity;
        this.navigator = navigator;
        this.categories = categories;
        this.products = products;
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
        refreshList();
    }

    @Override public void onEdit(Product product) { showProductForm(product); }

    @Override public void onDelete(Product product) {
        new AlertDialog.Builder(activity)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa \"" + product.name + "\" khỏi danh mục " + category.name + " không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    products.delete(product);
                    refreshList();
                    Toast.makeText(activity, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void showProductForm(Product product) {
        boolean editing = product != null;
        View form = LayoutInflater.from(activity).inflate(R.layout.product_form, null, false);
        EditText name = form.findViewById(R.id.input_product_name);
        EditText price = form.findViewById(R.id.input_product_price);
        EditText description = form.findViewById(R.id.input_product_description);
        ImageView preview = form.findViewById(R.id.product_image_preview);
        TextView chooseImage = form.findViewById(R.id.choose_product_image);
        String[] imageUri = {editing ? product.imageUri : Product.DEFAULT_IMAGE_URI};
        ProductImageLoader.load(preview, imageUri[0]);
        chooseImage.setOnClickListener(v -> navigator.pickProductImage(uri -> {
            imageUri[0] = uri.toString();
            ProductImageLoader.load(preview, uri.toString());
        }));
        if (editing) {
            name.setText(product.name);
            price.setText(NumberFormat.getNumberInstance(Locale.US).format(product.price));
            description.setText(product.description);
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(editing ? "Sửa sản phẩm" : "Thêm sản phẩm")
                .setView(form).setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu" : "Thêm", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String productName = name.getText().toString().trim();
            String priceText = price.getText().toString().trim().replace(",", "").replace(".", "");
            if (productName.isEmpty()) { name.setError("Vui lòng nhập tên sản phẩm"); return; }
            if (categories.getAll().stream().noneMatch(item -> item.id == category.id)) {
                Toast.makeText(activity, "Danh mục này không còn tồn tại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                navigator.showCategories();
                return;
            }
            if (products.containsName(category, productName, product)) { name.setError("Tên sản phẩm đã tồn tại trong danh mục"); return; }
            double amount;
            try { amount = Double.parseDouble(priceText); }
            catch (NumberFormatException ex) { price.setError("Vui lòng nhập giá hợp lệ"); return; }
            if (amount <= 0) { price.setError("Giá phải lớn hơn 0"); return; }
            String productDescription = description.getText().toString().trim();
            if (editing) products.update(product, productName, amount, productDescription, imageUri[0]);
            else products.add(category, productName, amount, productDescription, imageUri[0]);
            refreshList();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void refreshList() {
        if (adapter != null) adapter.submitList(products.getByCategory(category));
        TextView count = activity.findViewById(R.id.product_count);
        if (count != null) count.setText(products.getByCategory(category).size() + " sản phẩm");
        View empty = activity.findViewById(R.id.products_empty);
        if (empty != null) empty.setVisibility(products.getByCategory(category).isEmpty() ? View.VISIBLE : View.GONE);
    }
}
