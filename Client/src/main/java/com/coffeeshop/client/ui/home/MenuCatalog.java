package com.coffeeshop.client.ui.home;

import com.coffeeshop.client.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MenuCatalog {
    private MenuCatalog() { }

    // Two initial products and prices follow the requested design. Other entries are sample data.
    public static List<DrinkProduct> getProducts() {
        return Collections.unmodifiableList(Arrays.asList(
                new DrinkProduct("iced_milk_coffee", "Cà phê sữa đá", "Đậm vị, thơm ngon mỗi ngày", "coffee", 29000, 4.9f,
                        R.drawable.product_iced_milk_coffee),
                new DrinkProduct("caramel_latte", "Latte caramel", "Espresso cùng sữa tươi", "coffee", 45000, 4.8f,
                        R.drawable.product_latte_caramel_transparent),
                new DrinkProduct("black_iced_coffee", "Cà phê đen đá", "Đậm đà, ít ngọt", "coffee", 25000, 4.8f,
                        R.drawable.coffee_brand_photo),
                new DrinkProduct("bac_xiu", "Bạc xỉu", "Sữa thơm, cà phê dịu nhẹ", "coffee", 35000, 4.7f,
                        R.drawable.product_latte_caramel_transparent),
                new DrinkProduct("peach_tea", "Trà đào cam sả", "Trà thanh mát, đào thơm", "tea", 39000, 4.8f,
                        R.drawable.img_peach_tea),
                new DrinkProduct("mango_smoothie", "Sinh tố xoài", "Xoài chín xay mịn", "smoothie", 42000, 4.8f,
                        R.drawable.img_mango_smoothie),
                new DrinkProduct("matcha_latte", "Matcha latte", "Matcha thơm dịu cùng sữa tươi", "coffee", 45000, 4.8f,
                        R.drawable.product_latte_caramel_transparent),
                new DrinkProduct("cold_brew", "Cold brew cam", "Cà phê ủ lạnh, hương cam nhẹ", "coffee", 39000, 4.9f,
                        R.drawable.product_iced_milk_coffee),
                new DrinkProduct("lychee_tea", "Trà vải hoa hồng", "Trà trái cây thanh mát", "tea", 39000, 4.8f,
                        R.drawable.img_peach_tea),
                new DrinkProduct("cocoa_ice", "Cacao đá xay", "Cacao đậm vị phủ kem mịn", "smoothie", 43000, 4.7f,
                        R.drawable.img_mango_smoothie),
                new DrinkProduct("croissant", "Bánh croissant bơ", "Bánh ngàn lớp nướng thơm", "snack", 32000, 4.9f,
                        R.drawable.img_snack_croissant),
                new DrinkProduct("tiramisu", "Tiramisu cà phê", "Bánh mềm vị cacao và cà phê", "snack", 42000, 4.9f,
                        R.drawable.img_snack_tiramisu),
                new DrinkProduct("chicken_sandwich", "Sandwich gà nướng", "Gà nướng, rau tươi và sốt nhà làm", "snack", 49000, 4.8f,
                        R.drawable.img_snack_sandwich),
                new DrinkProduct("choco_cookie", "Cookie chocolate", "Bánh quy bơ với chocolate chip", "snack", 25000, 4.8f,
                        R.drawable.img_snack_cookie)
        ));
    }
}
