package com.coffeeshop.client.data;

import com.coffeeshop.client.R;
import com.coffeeshop.client.model.Category;
import com.coffeeshop.client.model.Product;
import com.coffeeshop.client.ui.home.DrinkProduct;
import com.coffeeshop.client.ui.home.MenuCatalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Local Home data, backed by the app's existing mock menu catalog. */
public final class MockData {
    private MockData() { }

    public static List<Category> getCategories() {
        return Collections.unmodifiableList(Arrays.asList(
                new Category("all", "Tất cả", R.drawable.ic_menu),
                new Category("coffee", "Cà phê", R.drawable.ic_splash_cup),
                new Category("tea", "Trà", R.drawable.ic_splash_cup),
                new Category("smoothie", "Sinh tố", R.drawable.img_mango_smoothie),
                new Category("snack", "Bánh", R.drawable.img_snack_croissant)
        ));
    }

    public static List<Product> getProducts() {
        List<Product> result = new ArrayList<>();
        for (DrinkProduct item : MenuCatalog.getProducts()) {
            result.add(new Product(item.id, item.category, item.name, item.description,
                    (double) item.priceVnd, item.rating, item.imageRes));
        }
        return Collections.unmodifiableList(result);
    }
}
