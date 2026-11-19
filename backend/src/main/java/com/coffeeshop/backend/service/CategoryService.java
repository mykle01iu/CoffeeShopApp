
package com.coffeeshop.backend.service;

import com.coffeeshop.backend.entity.Category;
import com.coffeeshop.backend.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 1. Lấy tất cả danh mục
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 2. Lấy danh mục theo ID
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Khong tim thay danh muc"
                ));
    }

    // 3. Thêm danh mục
    @Transactional
    public Category createCategory(Category request) {
        String code = normalizeCode(request.getCode());
        String name = validateName(request.getName());

        if (categoryRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ma danh muc da ton tai"
            );
        }

        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        category.setDescription(request.getDescription());
        category.setAvailable(
                request.getAvailable() != null
                        ? request.getAvailable() : true
        );

        return categoryRepository.save(category);
    }

    // 4. Cập nhật danh mục
    @Transactional
    public Category updateCategory(Long id, Category request) {
        Category category = getCategoryById(id);

        String code = normalizeCode(request.getCode());
        String name = validateName(request.getName());

        categoryRepository.findByCodeIgnoreCase(code)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ma danh muc da ton tai"
                        );
                    }
                });

        category.setCode(code);
        category.setName(name);
        category.setDescription(request.getDescription());

        if (request.getAvailable() != null) {
            category.setAvailable(request.getAvailable());
        }

        return categoryRepository.save(category);
    }

    // 5. Xóa danh mục
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

    // 6. Kiểm tra mã danh mục
    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma danh muc khong duoc de trong"
            );
        }

        String normalized = code.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma danh muc toi da 50 ky tu"
            );
        }

        return normalized;
    }

    // 7. Kiểm tra tên danh mục
    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten danh muc khong duoc de trong"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten danh muc toi da 255 ky tu"
            );
        }

        return normalized;
    }
}
