
package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 1. Lay tat ca san pham
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        List<Product> products;
        if (category == null || category.isBlank()) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductsByCategory(category);
        }
        return ResponseEntity.ok(products.stream()
                .map(product -> ProductResponse.from(product, request))
                .toList());
    }

    // 2. Lay san pham theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            HttpServletRequest request) {

        return ResponseEntity.ok(ProductResponse.from(
                productService.getProductById(id), request));
    }

    // 3. Them san pham - ADMIN
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        return ResponseEntity.ok(
                productService.createProduct(product)
        );
    }

    // 4. Cap nhat san pham - ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return ResponseEntity.ok(
                productService.updateProduct(id, product)
        );
    }

    // 5. Xoa san pham - ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                Map.of("message", "Xoa san pham thanh cong")
        );
    }
}
