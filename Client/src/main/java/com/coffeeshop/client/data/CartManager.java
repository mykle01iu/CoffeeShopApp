package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.coffeeshop.client.ui.home.CartOptions;
import com.coffeeshop.client.ui.home.DrinkProduct;

/** Home badge facade over the same repository used by CartActivity and CheckoutActivity. */
public final class CartManager {
    private CartManager() { }

    public static int getTotalItemCount(Context context) {
        return CartRepository.totalQuantity(context);
    }

    public static void addItem(Context context, DrinkProduct product, CartOptions.Selection option) {
        CartRepository.addSelection(context, product, option);
    }

    public static void updateItemQuantity(Context context, String itemKey, int delta) {
        if (context == null || itemKey == null) return;
        SharedPreferences prefs = CartRepository.getCartPreferences(context);
        int updated = prefs.getInt(itemKey, 0) + delta;
        if (updated <= 0) prefs.edit().remove(itemKey).apply();
        else prefs.edit().putInt(itemKey, updated).apply();
    }

    public static void removeItem(Context context, String itemKey) {
        if (context == null || itemKey == null) return;
        CartRepository.getCartPreferences(context).edit().remove(itemKey).apply();
    }

    public static void clearCart(Context context) {
        CartRepository.clear(context);
    }
}
