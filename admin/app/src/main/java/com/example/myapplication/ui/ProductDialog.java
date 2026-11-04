package com.example.myapplication.ui;

import android.app.Activity;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.data.ProductRepository;
import com.example.myapplication.model.Product;

public final class ProductDialog {

    private ProductDialog() {
    }

    // index < 0 nghĩa là thêm mới
    public static void show(Activity activity, ProductRepository repo, int index, Runnable onChanged) {
        boolean editing = index >= 0;
        Product old = editing ? repo.get(index) : null;

        LinearLayout form = new LinearLayout(activity);
        form.setPadding(UiUtils.dp(activity, 22), UiUtils.dp(activity, 8), UiUtils.dp(activity, 22), 0);
        form.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(activity);
        name.setSingleLine(true);
        name.setHint("Tên sản phẩm");
        name.setText(editing ? old.name : "");
        form.addView(name);

        EditText price = new EditText(activity);
        price.setSingleLine(true);
        price.setHint("Giá bán (VNĐ)");
        price.setInputType(InputType.TYPE_CLASS_NUMBER);
        price.setText(editing ? String.valueOf(old.price) : "");
        form.addView(price);

        Spinner category = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, ProductRepository.VALID_CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);
        if (editing) {
            for (int i = 0; i < ProductRepository.VALID_CATEGORIES.length; i++) {
                if (ProductRepository.VALID_CATEGORIES[i].equals(old.category)) {
                    category.setSelection(i);
                    break;
                }
            }
        }
        form.addView(category);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(editing ? "Cập nhật sản phẩm" : "Thêm sản phẩm")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing ? "Lưu thay đổi" : "Thêm mới", null)
                .setNeutralButton(editing ? "Xóa" : null, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String n = name.getText().toString().trim();
                String c = (String) category.getSelectedItem();
                String priceText = price.getText().toString().trim();

                if (n.isEmpty() || c == null || priceText.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int p;
                try {
                    p = Integer.parseInt(priceText);
                } catch (NumberFormatException ex) {
                    Toast.makeText(activity, "Giá không hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (p <= 0) {
                    Toast.makeText(activity, "Giá phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editing) {
                    old.name = n;
                    old.price = p;
                    old.category = c;
                } else {
                    repo.add(new Product(n, c, p));
                }

                onChanged.run();
                dialog.dismiss();
                Toast.makeText(activity,
                        editing ? "Đã cập nhật sản phẩm" : "Đã thêm sản phẩm",
                        Toast.LENGTH_SHORT).show();
            });

            if (editing) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    dialog.dismiss();
                    confirmDelete(activity, repo, index, onChanged);
                });
            }
        });

        dialog.show();
    }

    public static void confirmDelete(Activity activity, ProductRepository repo, int index, Runnable onChanged) {
        if (index < 0 || index >= repo.size()) return;

        new AlertDialog.Builder(activity)
                .setTitle("Xóa sản phẩm?")
                .setMessage("Bạn muốn xóa “" + repo.get(index).name + "” khỏi thực đơn?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repo.remove(index);
                    onChanged.run();
                    Toast.makeText(activity, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}