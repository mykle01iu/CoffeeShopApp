package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Size and preparation choices shared by menu, cart and checkout. */
public final class CartOptions {
    public static final String[] SIZES = {"S", "M", "L"};
    private static final String[] ICE_CODES = {"separate", "less", "normal", "none"};
    private static final String[] ICE_LABELS = {"Đá riêng", "Ít đá", "Đá bình thường", "Không đá"};
    private static final String[] SUGAR_CODES = {"0", "50", "70", "100"};
    private static final String[] SUGAR_LABELS = {"0%", "50%", "70%", "100%"};
    private static final String[] TEA_CODES = {"less", "normal", "more"};
    private static final String[] TEA_LABELS = {"Ít trà", "Lượng trà bình thường", "Nhiều trà"};
    private static final String[] TOPPING_CODES = {"espresso", "salt", "pearls"};
    private static final String[] TOPPING_LABELS = {"Thêm shot espresso (+10.000đ)", "Kem muối (+8.000đ)", "Trân châu (+8.000đ)"};

    private CartOptions() { }

    public static int price(DrinkProduct product, String size) {
        if ("snack".equals(product.category)) return product.priceVnd;
        if ("S".equals(size)) return product.priceVnd;
        if ("M".equals(size)) return product.priceVnd + 6000;
        if ("L".equals(size)) return product.priceVnd + 10000;
        return product.priceVnd;
    }

    public static int price(DrinkProduct product, Selection selection) {
        if ("snack".equals(product.category)) return product.priceVnd;
        int total = price(product, selection.size);
        for (String topping : selection.toppings.split(",")) {
            if ("espresso".equals(topping)) total += 10000;
            else if ("salt".equals(topping) || "pearls".equals(topping)) total += 8000;
        }
        return total;
    }

    public static String key(String productId, Selection selection) {
        return "cart_item_" + productId + "|" + selection.size + "|" + selection.ice
                + "|" + selection.sugar + "|" + selection.tea + "|" + selection.toppings;
    }

    public static List<Selection> selections(SharedPreferences prefs, DrinkProduct product) {
        List<Selection> result = new ArrayList<>();
        String prefix = "cart_item_" + product.id + "|";
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (!entry.getKey().startsWith(prefix) || !(entry.getValue() instanceof Integer)) continue;
            Selection selection = parseToken(entry.getKey().substring(prefix.length()));
            if (selection != null && (Integer) entry.getValue() > 0) {
                selection.quantity = (Integer) entry.getValue();
                result.add(selection);
            }
        }
        // Read older size-only cart entries as default preparation options.
        for (String size : SIZES) {
            int quantity = prefs.getInt("cart_" + product.id + "_" + size, 0);
            if (quantity > 0) {
                Selection selection = new Selection(size, "normal", "100", "normal", "");
                selection.quantity = quantity;
                result.add(selection);
            }
        }
        int oldQuantity = prefs.getInt("cart_" + product.id, 0);
        if (oldQuantity > 0) {
            Selection selection = new Selection("M", "normal", "100", "normal", "");
            selection.quantity = oldQuantity;
            result.add(selection);
        }
        return result;
    }

    public static void migrateOldCart(SharedPreferences prefs, List<DrinkProduct> products) {
        SharedPreferences.Editor edit = prefs.edit();
        boolean changed = false;
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (!entry.getKey().startsWith("cart_item_") || !(entry.getValue() instanceof Integer)) continue;
            String suffix = entry.getKey().substring("cart_item_".length());
            int divider = suffix.indexOf('|');
            if (divider < 0) continue;
            Selection old = parseToken(suffix.substring(divider + 1));
            if (old == null) continue;
            String migratedKey = key(suffix.substring(0, divider), old);
            if (!migratedKey.equals(entry.getKey())) {
                edit.putInt(migratedKey, prefs.getInt(migratedKey, 0) + (Integer) entry.getValue());
                edit.remove(entry.getKey());
                changed = true;
            }
        }
        for (DrinkProduct product : products) {
            int legacy = prefs.getInt("cart_" + product.id, 0);
            if (legacy > 0) {
                Selection selection = new Selection("M", "normal", "100", "normal", "");
                String key = key(product.id, selection);
                edit.putInt(key, prefs.getInt(key, 0) + legacy).remove("cart_" + product.id);
                changed = true;
            }
            for (String size : SIZES) {
                String oldKey = "cart_" + product.id + "_" + size;
                int quantity = prefs.getInt(oldKey, 0);
                if (quantity > 0) {
                    Selection selection = new Selection(size, "normal", "100", "normal", "");
                    String key = key(product.id, selection);
                    edit.putInt(key, prefs.getInt(key, 0) + quantity).remove(oldKey);
                    changed = true;
                }
            }
        }
        if (changed) edit.apply();
    }

    public static Selection parseToken(String serialized) {
        String[] fields = serialized.split("\\|", -1);
        if (fields.length != 5) return null;
        String sugar = fields[2];
        if ("normal".equals(sugar)) sugar = "100";
        else if ("less".equals(sugar)) sugar = "50";
        else if ("more".equals(sugar)) sugar = "70";
        String toppings = fields[4].replace("cheese", "salt").replace("jelly", "pearls");
        return new Selection(fields[0], fields[1], sugar, fields[3], toppings);
    }

    public static String description(Selection selection) {
        return "Size " + selection.size + " · " + label(ICE_CODES, ICE_LABELS, selection.ice)
                + " · " + label(SUGAR_CODES, SUGAR_LABELS, selection.sugar)
                + " · " + label(TEA_CODES, TEA_LABELS, selection.tea)
                + (selection.toppings.isEmpty() ? " · Không topping" : " · " + toppingDescription(selection.toppings));
    }

    private static String label(String[] codes, String[] labels, String code) {
        for (int i = 0; i < codes.length; i++) if (codes[i].equals(code)) return labels[i];
        return labels[0];
    }
    private static String toppingDescription(String codes) {
        List<String> labels = new ArrayList<>();
        for (String code : codes.split(",")) for (int i = 0; i < TOPPING_CODES.length; i++)
            if (TOPPING_CODES[i].equals(code)) labels.add(TOPPING_LABELS[i].replaceAll(" \\([^)]+\\)", ""));
        return TextUtils.join(", ", labels);
    }

    public static void chooseSize(Context context, DrinkProduct product, OnSelectionSelected listener) {
        chooseOptions(context, product, null, listener);
    }

    public static void editOptions(Context context, DrinkProduct product, Selection current, OnSelectionSelected listener) {
        chooseOptions(context, product, current, listener);
    }

    private static void chooseOptions(Context context, DrinkProduct product, Selection current, OnSelectionSelected listener) {
        String[] sizeOptions = new String[SIZES.length];
        for (int i = 0; i < SIZES.length; i++)
            sizeOptions[i] = "Size " + SIZES[i] + "  ·  " + String.format(Locale.getDefault(), "%,dđ", price(product, SIZES[i]));
        ScrollView scroll = new ScrollView(context);
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(context, 22), dp(context, 8), dp(context, 22), dp(context, 12));
        scroll.addView(form, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] selectedSize = {indexOf(SIZES, current == null ? "S" : current.size, 0)};
        int[] selectedIce = {indexOf(ICE_CODES, current == null ? "normal" : current.ice, 2)};
        int[] selectedSugar = {indexOf(SUGAR_CODES, current == null ? "100" : current.sugar, 3)};
        int[] selectedTea = {indexOf(TEA_CODES, current == null ? "normal" : current.tea, 1)};
        RadioGroup sizeGroup = addRadioGroup(context, form, "Chọn size", sizeOptions, selectedSize[0], selectedSize);
        RadioGroup iceGroup = addRadioGroup(context, form, "Mức đá", ICE_LABELS, selectedIce[0], selectedIce);
        RadioGroup sugarGroup = addRadioGroup(context, form, "Mức đường", SUGAR_LABELS, selectedSugar[0], selectedSugar);
        RadioGroup teaGroup = addRadioGroup(context, form, "Lượng trà", TEA_LABELS, selectedTea[0], selectedTea);
        boolean[] checked = new boolean[TOPPING_CODES.length];
        addHeading(context, form, "Topping · có thể chọn nhiều");
        for (int i = 0; i < TOPPING_LABELS.length; i++) {
            final int index = i;
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(TOPPING_LABELS[i]);
            checkBox.setTextSize(14);
            checkBox.setChecked(current != null && java.util.Arrays.asList(current.toppings.split(",")).contains(TOPPING_CODES[i]));
            checkBox.setOnCheckedChangeListener((button, isChecked) -> checked[index] = isChecked);
            checked[i] = checkBox.isChecked();
            form.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 44)));
        }
        new AlertDialog.Builder(context).setTitle((current == null ? "Tùy chỉnh món · " : "Sửa món · ") + product.name)
                .setView(scroll)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(current == null ? "Thêm vào giỏ" : "Cập nhật", (dialog, which) -> {
                    selectedSize[0] = selectedIndex(sizeGroup, SIZES.length, 0);
                    selectedIce[0] = selectedIndex(iceGroup, ICE_LABELS.length, 2);
                    selectedSugar[0] = selectedIndex(sugarGroup, SUGAR_LABELS.length, 3);
                    selectedTea[0] = selectedIndex(teaGroup, TEA_LABELS.length, 1);
                    List<String> toppings = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) if (checked[i]) toppings.add(TOPPING_CODES[i]);
                    listener.onSelected(new Selection(SIZES[selectedSize[0]], ICE_CODES[selectedIce[0]], SUGAR_CODES[selectedSugar[0]],
                            TEA_CODES[selectedTea[0]], TextUtils.join(",", toppings)));
                }).show();
    }

    private static int indexOf(String[] values, String value, int fallback) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return i;
        return fallback;
    }

    private static RadioGroup addRadioGroup(Context context, LinearLayout form, String title, String[] labels,
                                           int defaultIndex, int[] selected) {
        addHeading(context, form, title);
        RadioGroup group = new RadioGroup(context);
        group.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            RadioButton button = new RadioButton(context);
            button.setId(1000 + form.getChildCount() * 10 + i);
            button.setText(labels[i]);
            button.setTextSize(14);
            group.addView(button, new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 42)));
            if (i == defaultIndex) group.check(button.getId());
        }
        group.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            for (int i = 0; i < radioGroup.getChildCount(); i++)
                if (radioGroup.getChildAt(i).getId() == checkedId) selected[0] = i;
        });
        form.addView(group);
        return group;
    }

    private static int selectedIndex(RadioGroup group, int optionCount, int fallback) {
        for (int i = 0; i < group.getChildCount(); i++)
            if (group.getChildAt(i).getId() == group.getCheckedRadioButtonId()) return i;
        return fallback < optionCount ? fallback : 0;
    }

    private static void addHeading(Context context, LinearLayout form, String text) {
        TextView heading = new TextView(context);
        heading.setText(text); heading.setTextSize(15); heading.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(context, 12); params.bottomMargin = dp(context, 4);
        form.addView(heading, params);
    }

    private static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public interface OnSelectionSelected { void onSelected(Selection selection); }

    public static final class Selection {
        public final String size, ice, sugar, tea, toppings;
        public int quantity;
        public Selection(String size, String ice, String sugar, String tea, String toppings) {
            this.size = size; this.ice = ice; this.sugar = sugar; this.tea = tea; this.toppings = toppings;
        }
        public String token() { return size + "|" + ice + "|" + sugar + "|" + tea + "|" + toppings; }
    }
}
