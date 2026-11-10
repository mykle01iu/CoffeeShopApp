package com.coffeeshop.client.ui.home;

import com.coffeeshop.client.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MenuCatalog {
    private MenuCatalog() { }

    public static List<DrinkProduct> getProducts() {
        return Collections.unmodifiableList(Arrays.asList(
                // 1. Nâu đá
                new DrinkProduct("nau_da", "Nâu đá", "Cà phê phin sữa đá đậm đà, thơm ngon mỗi ngày.", "coffee", 29000, 4.9f, R.drawable.nauda),
                // 2. Bạc xỉu
                new DrinkProduct("bac_xiu", "Bạc xỉu", "Sữa thơm hòa cùng cà phê dịu nhẹ.", "coffee", 35000, 4.8f, R.drawable.bacxiu),
                // 3. Cà phê phin
                new DrinkProduct("cafe_phin", "Cà phê phin", "Đậm đà, chuẩn vị truyền thống.", "coffee", 25000, 4.9f, R.drawable.caphephin),

                // 4. Trà xoài chanh leo
                new DrinkProduct("tra_xoai_chanh_day", "Trà xoài chanh leo", "Xoài thơm kết hợp chanh leo chua ngọt.", "tea", 39000, 4.9f, R.drawable.traxoaichanhday1),
                // 5. Trà sen vàng
                new DrinkProduct("tra_sen_vang", "Trà sen vàng", "Trà sen thơm dịu, hạt sen bùi.", "tea", 45000, 4.9f, R.drawable.trasenvang),
                // 6. Trà cóc xí muội
                new DrinkProduct("tra_coc_xi_muoi", "Trà cóc xí muội", "Vị chua ngọt đậm đà độc đáo.", "tea", 35000, 4.8f, R.drawable.tracocximuoi),
                // 7. Trà đào cam sả
                new DrinkProduct("tra_dao_cam_xa", "Trà đào cam sả", "Trà thanh mát với đào thơm và hương sả.", "tea", 39000, 4.9f, R.drawable.tradaocamxa),
                // 8. Trà dưa lưới
                new DrinkProduct("tra_dua_luoi", "Trà dưa lưới", "Hương dưa lưới thơm ngọt thanh.", "tea", 35000, 4.7f, R.drawable.tradualuoi),
                // 9. Trà thạch đào
                new DrinkProduct("tra_thach_dao", "Trà thạch đào", "Trà đào thơm với thạch giòn sần sật.", "tea", 39000, 4.8f, R.drawable.trathachdao),
                // 10. Trà vải
                new DrinkProduct("tra_vai", "Trà vải", "Trà vải thanh ngọt, mọng nước.", "tea", 39000, 4.8f, R.drawable.travai),

                // 11. Cacao đá xay
                new DrinkProduct("cacao_da_xay", "Cacao đá xay", "Cacao đậm vị phủ kem mịn.", "smoothie", 43000, 4.8f, R.drawable.cacaodaxay),
                // 12. Matcha latte
                new DrinkProduct("matcha_latte", "Matcha latte", "Matcha thơm dịu cùng sữa tươi.", "latte", 45000, 4.8f, R.drawable.matchalatte),

                // 13. Cheese Cake
                new DrinkProduct("cheese_cake", "Cheese Cake", "Bánh phô mai béo ngậy, mềm mịn.", "snack", 45000, 4.9f, R.drawable.cheesecake),
                // 14. Croissant
                new DrinkProduct("croissant", "Croissant", "Bánh ngàn lớp nướng thơm bơ.", "snack", 32000, 4.9f, R.drawable.croissant),
                // 15. Tiramisu
                new DrinkProduct("tiramisu", "Tiramisu", "Bánh mềm vị cacao và cà phê.", "snack", 42000, 4.9f, R.drawable.timarisu)
        ));
    }
}
