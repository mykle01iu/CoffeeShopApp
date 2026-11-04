package com.example.myapplication.data;

import com.example.myapplication.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductRepository {

    public static final String CAT_ALL = "Tất cả";
    public static final String CAT_COFFEE = "Cà phê";
    public static final String CAT_TEA = "Trà";
    public static final String CAT_OTHER = "Khác";
    public static final String[] VALID_CATEGORIES = {CAT_COFFEE, CAT_TEA, CAT_OTHER};

    private final ArrayList<Product> products = new ArrayList<>();

    public ProductRepository() {
        seed();
    }

    public List<Product> getAll() {
        return Collections.unmodifiableList(products);
    }

    public int size() {
        return products.size();
    }

    public Product get(int index) {
        return products.get(index);
    }

    // Sản phẩm mới luôn được thêm lên đầu danh sách
    public void add(Product product) {
        products.add(0, product);
    }

    public void remove(int index) {
        products.remove(index);
    }

    private void seed() {
        products.add(new Product("Cà phê sữa đá", CAT_COFFEE, 35000));
        products.add(new Product("Bạc xỉu", CAT_COFFEE, 39000));
        products.add(new Product("Americano", CAT_COFFEE, 42000));
        products.add(new Product("Cappuccino", CAT_COFFEE, 55000));
        products.add(new Product("Trà đào cam sả", CAT_TEA, 49000));
        products.add(new Product("Trà sen vàng", CAT_TEA, 45000));
        products.add(new Product("Trà vải", CAT_TEA, 46000));
        products.add(new Product("Bánh croissant", CAT_OTHER, 32000));
    }
}