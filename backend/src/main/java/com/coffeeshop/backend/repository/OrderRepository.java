
package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    // Lay don hang cua mot User, moi nhat truoc
    @EntityGraph(attributePaths = {"user", "details"})
    List<Order> findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    // Lay tat ca don hang cho Admin
    @EntityGraph(attributePaths = {"user", "details"})
    List<Order> findAllByOrderByCreatedAtDesc();
}
