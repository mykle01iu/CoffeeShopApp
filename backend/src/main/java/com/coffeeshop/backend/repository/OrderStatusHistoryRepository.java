
package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, Long> {

    // Lay lich su trang thai cua mot don hang
    // Sap xep tu cu den moi
    List<OrderStatusHistory>
        findByOrder_IdOrderByChangedAtAscIdAsc(Long orderId);
}
