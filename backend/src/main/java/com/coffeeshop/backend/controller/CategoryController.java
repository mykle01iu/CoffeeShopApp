
package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.entity.Category;
import com.coffeeshop.backend.service.CategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 1. Lấy tất cả danh mục
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // 2. Lấy danh mục theo ID
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    // 3. Thêm danh mục
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody Category request) {

        Category created = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // 4. Cập nhật danh mục
    @PutMapping("/{id}")
    public Category updateCategory(
            @PathVariable Long id,
            @RequestBody Category request) {

        return categoryService.updateCategory(id, request);
    }

    // 5. Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
