package com.example.myapplication.data;

import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory product data for the category management exercise. */
public class ProductRepository {
    private final ArrayList<Product> products = new ArrayList<>();
    private int nextId = 7;

    public ProductRepository() {
        products.add(new Product(1, 1, "Cà phê sữa đá", 29000, "Cà phê đậm đà cùng sữa đặc.", Product.DEFAULT_IMAGE_URI));
        products.add(new Product(2, 1, "Bạc xỉu", 32000, "Nhiều sữa, thơm vị cà phê.", Product.DEFAULT_IMAGE_URI));
        products.add(new Product(3, 1, "Americano", 35000, "Cà phê đen phong cách Ý.", Product.DEFAULT_IMAGE_URI));
        products.add(new Product(4, 2, "Trà đào cam sả", 39000, "Trà trái cây tươi mát.", Product.DEFAULT_IMAGE_URI));
        products.add(new Product(5, 2, "Trà sữa trân châu", 42000, "Trà sữa béo nhẹ, trân châu dai.", Product.DEFAULT_IMAGE_URI));
        products.add(new Product(6, 3, "Bánh tiramisu", 45000, "Bánh mềm với hương cà phê.", Product.DEFAULT_IMAGE_URI));
    }

    public List<Product> getByCategory(Category category) {
        ArrayList<Product> result = new ArrayList<>();
        for (Product product : products) if (product.categoryId == category.id) result.add(product);
        return Collections.unmodifiableList(result);
    }

    public void add(Category category, String name, double price, String description, String imageUri) {
        products.add(new Product(nextId++, category.id, name, price, description, imageUri));
    }

    public void update(Product product, String name, double price, String description, String imageUri) {
        product.name = name;
        product.price = price;
        product.description = description;
        product.imageUri = imageUri;
    }

    public void delete(Product product) { products.remove(product); }

    public boolean containsName(Category category, String name, Product except) {
        for (Product product : products) {
            if (product.categoryId == category.id && product != except && product.name.equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }
}
