package com.coffeeshop.client.ui.common;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.SparseArray;

public final class FontUtils {
    private static final String INTER_ASSET = "fonts/Inter[opsz,wght].ttf";
    private static final String LORA_ASSET = "fonts/Lora[wght].ttf";
    private static final SparseArray<Typeface> INTER_CACHE = new SparseArray<>();
    private static final SparseArray<Typeface> LORA_CACHE = new SparseArray<>();

    private FontUtils() { }

    public static Typeface inter(Context context, int weight) {
        return typeface(context, INTER_ASSET, weight);
    }

    public static Typeface lora(Context context, int weight) {
        return typeface(context, LORA_ASSET, weight);
    }

    public static void applyInter(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int currentStyle = textView.getTypeface() == null ? Typeface.NORMAL : textView.getTypeface().getStyle();
            int weight = (currentStyle & Typeface.BOLD) != 0 ? 600 : 400;
            textView.setTypeface(inter(textView.getContext(), weight));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyInter(group.getChildAt(index));
            }
        }
    }

    private static Typeface typeface(Context context, String asset, int weight) {
        SparseArray<Typeface> cache = asset.equals(INTER_ASSET) ? INTER_CACHE : LORA_CACHE;
        Typeface cached = cache.get(weight);
        if (cached != null) return cached;

        Typeface result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String variations = asset.equals(LORA_ASSET)
                    ? "'wght' " + weight
                    : "'wght' " + weight + ", 'opsz' 14";
            result = new Typeface.Builder(context.getAssets(), asset)
                    .setFontVariationSettings(variations)
                    .build();
        } else {
            Typeface base = Typeface.createFromAsset(context.getAssets(), asset);
            result = Typeface.create(base, weight >= 600 ? Typeface.BOLD : Typeface.NORMAL);
        }
        cache.put(weight, result);
        return result;
    }
}
