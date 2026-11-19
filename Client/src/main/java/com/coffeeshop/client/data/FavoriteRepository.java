package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.coffeeshop.client.catalog.data.ProductRepository;
import com.coffeeshop.client.catalog.model.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FavoriteRepository {
    private static final String PREF_NAME = "moc_mien_favorites";
    private static final String KEY_FAVORITES = "favorite_product_ids";

    private FavoriteRepository() { }

    public static Set<String> getFavoriteIdsAsStrings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    public static boolean isFavorite(Context context, int productId) {
        Set<String> set = getFavoriteIdsAsStrings(context);
        return set.contains(String.valueOf(productId));
    }

    public static boolean toggleFavorite(Context context, int productId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
        String idStr = String.valueOf(productId);
        boolean isNowFavorite;
        if (set.contains(idStr)) {
            set.remove(idStr);
            isNowFavorite = false;
        } else {
            set.add(idStr);
            isNowFavorite = true;
        }
        prefs.edit().putStringSet(KEY_FAVORITES, set).apply();
        return isNowFavorite;
    }

    public static List<Product> getFavoriteProducts(Context context) {
        Set<String> favIds = getFavoriteIdsAsStrings(context);
        List<Product> result = new ArrayList<>();
        for (Product p : ProductRepository.getProducts()) {
            if (favIds.contains(String.valueOf(p.id))) {
                result.add(p);
            }
        }
        return result;
    }
}
