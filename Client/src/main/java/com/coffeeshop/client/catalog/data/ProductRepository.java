package com.coffeeshop.client.catalog.data;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProductRepository {
    private ProductRepository() { }
    public static List<Product> getProducts() {
        return Collections.unmodifiableList(Arrays.asList(
            new Product(1, "Nâu đá", "Cà phê", 29000, 4.9f, 35, "Cà phê phin sữa đá đậm đà, thơm ngon mỗi ngày.", R.drawable.nauda),
            new Product(2, "Bạc xỉu", "Cà phê", 35000, 4.8f, 28, "Sữa thơm hòa cùng cà phê dịu nhẹ.", R.drawable.bacxiu),
            new Product(3, "Cà phê phin", "Cà phê", 25000, 4.9f, 22, "Đậm đà, chuẩn vị truyền thống.", R.drawable.caphephin),
            new Product(4, "Trà xoài chanh leo", "Trà", 39000, 4.9f, 48, "Xoài thơm kết hợp chanh leo chua ngọt.", R.drawable.traxoaichanhday1),
            new Product(5, "Trà sen vàng", "Trà", 45000, 4.9f, 42, "Trà sen thơm dịu, hạt sen bùi.", R.drawable.trasenvang),
            new Product(6, "Trà cóc xí muội", "Trà", 35000, 4.8f, 16, "Vị chua ngọt đậm đà độc đáo.", R.drawable.tracocximuoi),
            new Product(7, "Trà đào cam sả", "Trà", 39000, 4.9f, 32, "Trà thanh mát với đào thơm và hương sả.", R.drawable.tradaocamxa),
            new Product(8, "Trà dưa lưới", "Trà", 35000, 4.7f, 14, "Hương dưa lưới thơm ngọt thanh.", R.drawable.tradualuoi),
            new Product(9, "Trà thạch đào", "Trà", 39000, 4.8f, 19, "Trà đào thơm với thạch giòn sần sật.", R.drawable.trathachdao),
            new Product(10, "Trà vải", "Trà", 39000, 4.8f, 20, "Trà vải thanh ngọt, mọng nước.", R.drawable.travai),
            new Product(11, "Cacao đá xay", "Đá xay", 43000, 4.8f, 26, "Cacao đậm vị phủ kem mịn.", R.drawable.cacaodaxay),
            new Product(12, "Matcha latte", "Khác", 45000, 4.8f, 18, "Matcha thơm dịu cùng sữa tươi.", R.drawable.matchalatte),
            new Product(13, "Cheese Cake", "Bánh", 45000, 4.9f, 15, "Bánh phô mai béo ngậy, mềm mịn.", R.drawable.cheesecake),
            new Product(14, "Croissant", "Bánh", 32000, 4.9f, 12, "Bánh ngàn lớp nướng thơm bơ.", R.drawable.croissant),
            new Product(15, "Tiramisu", "Bánh", 42000, 4.9f, 17, "Bánh mềm vị cacao và cà phê.", R.drawable.timarisu)
        ));
    }
}
