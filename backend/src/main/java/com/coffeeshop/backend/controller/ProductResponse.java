package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.config.ProductCatalogRepair;
import com.coffeeshop.backend.config.ProductCatalogRepair.CatalogItem;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category,
        Boolean available,
        LocalDateTime createdAt) {

    static ProductResponse from(Product product, HttpServletRequest request) {
        CatalogItem catalogItem = ProductCatalogRepair.catalogItem(product.getId());
        String name = product.getName();
        String description = product.getDescription();
        if (catalogItem != null) {
            if (ProductCatalogRepair.isMojibake(name)) name = catalogItem.name();
            if (ProductCatalogRepair.isMojibake(description)) description = catalogItem.description();
        }
        String fileName = imageFileName(product.getId());
        String imageUrl = product.getImageUrl();
        if (fileName != null) {
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            int port = request.getServerPort();
            if (!(request.getScheme().equals("http") && port == 80)
                    && !(request.getScheme().equals("https") && port == 443)) {
                baseUrl += ":" + port;
            }
            imageUrl = baseUrl + request.getContextPath()
                    + "/images/products/" + fileName;
        }
        return new ProductResponse(product.getId(), name,
                description, product.getPrice(), imageUrl,
                product.getCategory(), product.getAvailable(), product.getCreatedAt());
    }

    private static String imageFileName(Long id) {
        if (id == null || id < 1 || id > 15) return null;
        return switch (id.intValue()) {
            case 1 -> "nau_da.png";
            case 2 -> "bac_xiu.png";
            case 3 -> "ca_phe_phin.png";
            case 4 -> "tra_xoai_chanh_leo.png";
            case 5 -> "tra_sen_vang.png";
            case 6 -> "tra_coc_xi_muoi.png";
            case 7 -> "tra_dao_cam_sa.png";
            case 8 -> "tra_dua_luoi.png";
            case 9 -> "tra_thach_dao.png";
            case 10 -> "tra_vai.png";
            case 11 -> "cacao_da_xay.png";
            case 12 -> "matcha_latte.png";
            case 13 -> "cheese_cake.png";
            case 14 -> "croissant.jpg";
            case 15 -> "tiramisu.png";
            default -> null;
        };
    }
}
