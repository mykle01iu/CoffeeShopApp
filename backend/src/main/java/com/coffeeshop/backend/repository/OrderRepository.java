
package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.Order;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    // Ngay 6: Lay don hang cua User
        @EntityGraph(attributePaths = {"user", "details"})
        List<Order> findByUser_IdOrderByCreatedAtDesc(
        Long userId
        );

    // Ngay 6: Lay tat ca don hang cho Admin
        @EntityGraph(attributePaths = {"user", "details"})
        List<Order> findAllByOrderByCreatedAtDesc();

    // Ngay 7: Khoa don hang trong luc cap nhat trang thai
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT o FROM CoffeeOrder o WHERE o.id = :id")
        Optional<Order> findByIdForStatusUpdate(
        @Param("id") Long id
        );
}
