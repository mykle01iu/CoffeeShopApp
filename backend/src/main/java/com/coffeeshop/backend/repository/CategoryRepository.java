
package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Tìm danh mục theo mã (không phân biệt chữ hoa/thường)
    Optional<Category> findByCodeIgnoreCase(String code);

    // Kiểm tra mã danh mục đã tồn tại chưa
    boolean existsByCodeIgnoreCase(String code);
}
