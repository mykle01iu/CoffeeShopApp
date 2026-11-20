package com.coffeeshop.backend.config;

import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.repository.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProductCatalogRepair implements ApplicationRunner {
    public record CatalogItem(long id, String name, String description, String image) { }

    private static final List<CatalogItem> CATALOG = List.of(
            new CatalogItem(1, "Nâu đá", "Cà phê phin sữa đá đậm đà, thơm ngon mỗi ngày.", "nau_da.png"),
            new CatalogItem(2, "Bạc xỉu", "Sữa thơm hòa cùng cà phê dịu nhẹ.", "bac_xiu.png"),
            new CatalogItem(3, "Cà phê phin", "Đậm đà, chuẩn vị truyền thống.", "ca_phe_phin.png"),
            new CatalogItem(4, "Trà xoài chanh leo", "Xoài thơm kết hợp chanh leo chua ngọt.", "tra_xoai_chanh_leo.png"),
            new CatalogItem(5, "Trà sen vàng", "Trà sen thơm dịu, hạt sen bùi.", "tra_sen_vang.png"),
            new CatalogItem(6, "Trà cóc xí muội", "Vị chua ngọt đậm đà độc đáo.", "tra_coc_xi_muoi.png"),
            new CatalogItem(7, "Trà đào cam sả", "Trà thanh mát với đào thơm và hương sả.", "tra_dao_cam_sa.png"),
            new CatalogItem(8, "Trà dưa lưới", "Hương dưa lưới thơm ngọt thanh.", "tra_dua_luoi.png"),
            new CatalogItem(9, "Trà thạch đào", "Trà đào thơm với thạch giòn sần sật.", "tra_thach_dao.png"),
            new CatalogItem(10, "Trà vải", "Trà vải thanh ngọt, mọng nước.", "tra_vai.png"),
            new CatalogItem(11, "Cacao đá xay", "Cacao đậm vị phủ kem mịn.", "cacao_da_xay.png"),
            new CatalogItem(12, "Matcha latte", "Matcha thơm dịu cùng sữa tươi.", "matcha_latte.png"),
            new CatalogItem(13, "Cheese Cake", "Bánh phô mai béo ngậy, mềm mịn.", "cheese_cake.png"),
            new CatalogItem(14, "Croissant", "Bánh ngàn lớp nướng thơm bơ.", "croissant.jpg"),
            new CatalogItem(15, "Tiramisu", "Bánh mềm vị cacao và cà phê.", "tiramisu.png")
    );

    private final ProductRepository products;

    public ProductCatalogRepair(ProductRepository products) {
        this.products = products;
    }

    public static CatalogItem catalogItem(Long id) {
        if (id == null) return null;
        return CATALOG.stream().filter(item -> item.id() == id).findFirst().orElse(null);
    }

    public static boolean isMojibake(String value) {
        return value != null && (value.contains("Ã") || value.contains("Ä")
                || value.contains("Æ") || value.contains("�"));
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (CatalogItem item : CATALOG) {
            products.findById(item.id()).ifPresent(product -> repair(product, item));
        }
    }

    private void repair(Product product, CatalogItem item) {
        if (isMojibake(product.getName())) product.setName(item.name());
        if (isMojibake(product.getDescription())) product.setDescription(item.description());
        if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
            product.setImageUrl("/images/products/" + item.image());
        }
    }

}
