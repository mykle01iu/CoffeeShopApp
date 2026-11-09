package com.example.myapplication.data;

import com.example.myapplication.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory mock data for category management. */
public class CategoryRepository {
    private final ArrayList<Category> categories = new ArrayList<>();
    private int nextId = 4;

    public CategoryRepository() {
        categories.add(new Category(1, "Cà phê", "Các loại cà phê nóng và lạnh"));
        categories.add(new Category(2, "Trà", "Trà trái cây và trà sữa"));
        categories.add(new Category(3, "Bánh ngọt", "Bánh dùng kèm đồ uống"));
    }

    public List<Category> getAll() { return Collections.unmodifiableList(categories); }

    public void add(String name, String description) {
        categories.add(new Category(nextId++, name, description));
    }

    public void update(Category category, String name, String description) {
        category.name = name;
        category.description = description;
    }

    public void delete(Category category) { categories.remove(category); }

    public boolean containsName(String name, Category except) {
        for (Category category : categories) {
            if (category != except && category.name.equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }
}
