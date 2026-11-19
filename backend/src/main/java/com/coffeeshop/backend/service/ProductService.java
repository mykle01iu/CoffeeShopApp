
package com.coffeeshop.backend.service;

import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.repository.ProductRepository;
import com.coffeeshop.backend.repository.CategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    private static final BigDecimal MAX_PRICE =
            new BigDecimal("99999999.99");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // 1. Lay tat ca san pham
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2. Lay san pham theo ID
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Khong tim thay san pham"
                ));
    }

    // 3. Lay san pham theo danh muc
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma danh muc khong hop le"
            );
        }

        return productRepository.findByCategory(
                category.trim().toUpperCase(Locale.ROOT)
        );
    }

    // 4. Lay san pham dang kinh doanh
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }

    // 5. Them san pham
    @Transactional
    public Product createProduct(Product request) {

        Product product = new Product();

        applyProductData(product, request, true);

        return productRepository.save(product);
    }

    // 6. Cap nhat san pham
    @Transactional
    public Product updateProduct(Long id, Product request) {

        Product product = getProductById(id);

        applyProductData(product, request, false);

        return productRepository.save(product);
    }

    // 7. Xoa san pham
    @Transactional
    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }

    // 8. Gan va kiem tra du lieu san pham
    private void applyProductData(
            Product product,
            Product request,
            boolean creating) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Du lieu san pham khong hop le"
            );
        }

        product.setName(validateName(request.getName()));

        product.setDescription(request.getDescription());

        product.setPrice(validatePrice(request.getPrice()));

        product.setImageUrl(
                validateImageUrl(request.getImageUrl())
        );

        product.setCategory(
                validateCategory(request.getCategory())
        );

        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        } else if (creating) {
            product.setAvailable(true);
        }
    }

    // 9. Kiem tra ten san pham
    private String validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten san pham khong duoc de trong"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten san pham toi da 255 ky tu"
            );
        }

        return normalized;
    }

    // 10. Kiem tra gia san pham
    private BigDecimal validatePrice(BigDecimal price) {

        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gia san pham phai lon hon 0"
            );
        }

        if (price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gia khong hop le"
            );
        }

        return price;
    }

    // 11. Kiem tra URL anh
    private String validateImageUrl(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String url = imageUrl.trim();

        if (url.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL anh toi da 255 ky tu"
            );
        }

        try {
            URI uri = URI.create(url);

            String scheme = uri.getScheme();

            if (scheme == null
                    || uri.getHost() == null
                    || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))) {

                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL anh phai la HTTP hoac HTTPS"
            );
        }

        return url;
    }

    // 12. Kiem tra danh muc co ton tai
    private String validateCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Danh muc khong duoc de trong"
            );
        }

        String code = category.trim()
                .toUpperCase(Locale.ROOT);

        if (code.length() > 50
                || !categoryRepository.existsByCodeIgnoreCase(code)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma danh muc khong ton tai hoac khong hop le"
            );
        }

        return code;
    }
}
