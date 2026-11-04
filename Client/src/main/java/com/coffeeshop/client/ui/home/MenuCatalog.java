package com.coffeeshop.client.ui.home;

import com.coffeeshop.client.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MenuCatalog {
    private MenuCatalog() { }

    public static List<DrinkProduct> getProducts() {
        return Collections.unmodifiableList(Arrays.asList(
                // Món Được yêu thích (Featured / Popular PR)
                new DrinkProduct("tra_xoai_chanh_day", "Trà xoài chanh leo", "Xoài thơm kết hợp chanh leo chua ngọt", "tea", 39000, 4.9f,
                        R.drawable.traxoaichanhleo),
                new DrinkProduct("tra_sen_vang", "Trà sen vàng", "Trà sen thơm dịu, hạt sen bùi", "tea", 45000, 4.9f,
                        R.drawable.trasenvang),

                // Cà phê
                new DrinkProduct("bac_xiu", "Bạc xỉu", "Sữa thơm, cà phê dịu nhẹ", "coffee", 35000, 4.8f,
                        R.drawable.bacxiu),
                new DrinkProduct("cafe_phin", "Cafe phin", "Đậm đà, chuẩn vị truyền thống", "coffee", 25000, 4.9f,
                        R.drawable.caphephin),
                new DrinkProduct("nau_da", "Nâu đá", "Cà phê phin sữa đá đậm đà", "coffee", 29000, 4.9f,
                        R.drawable.nauda),

                // Trà
                new DrinkProduct("tra_chanh", "Trà chanh", "Chua ngọt thanh mát, giải nhiệt", "tea", 25000, 4.8f,
                        R.drawable.trachanh),
                new DrinkProduct("tra_coc_xi_muoi", "Trà cóc xí muội", "Vị chua ngọt đậm đà độc đáo", "tea", 35000, 4.8f,
                        R.drawable.tracocximuoi),
                new DrinkProduct("peach_tea", "Trà đào cam sả", "Trà thanh mát, đào thơm", "tea", 39000, 4.9f,
                        R.drawable.tradaocamxa),
                new DrinkProduct("tra_dua_luoi", "Trà dưa lưới", "Hương dưa lưới thơm ngọt thanh", "tea", 35000, 4.7f,
                        R.drawable.tradualuoi),
                new DrinkProduct("tra_thach_dao", "Trà thạch đào", "Trà đào thơm giòn sần sật", "tea", 39000, 4.8f,
                        R.drawable.trathachdao),
                new DrinkProduct("lychee_tea", "Trà vải", "Trà vải thanh ngọt mọng nước", "tea", 39000, 4.8f,
                        R.drawable.travai),

                // Đá xay
                new DrinkProduct("cocoa_ice", "Cacao đá xay", "Cacao đậm vị phủ kem mịn", "smoothie", 43000, 4.8f,
                        R.drawable.cacaodaxay),

                // Latte
                new DrinkProduct("matcha_latte", "Matcha latte", "Matcha thơm dịu cùng sữa tươi", "latte", 45000, 4.8f,
                        R.drawable.matchalatte),

                // Bánh ngọt
                new DrinkProduct("cheese_cake", "Cheese Cake", "Bánh phô mai béo ngậy, mềm mịn", "snack", 45000, 4.9f,
                        R.drawable.cheesecake),
                new DrinkProduct("croissant", "Croissant", "Bánh ngàn lớp nướng thơm bơ", "snack", 32000, 4.9f,
                        R.drawable.croissant),
                new DrinkProduct("tiramisu", "Tiramisu", "Bánh mềm vị cacao và cà phê", "snack", 42000, 4.9f,
                        R.drawable.timarisu)
        ));
    }
}
