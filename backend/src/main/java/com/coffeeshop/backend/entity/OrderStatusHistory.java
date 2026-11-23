
package com.coffeeshop.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "order_status_history",
    indexes = @Index(
        name = "idx_order_status_history_order",
        columnList = "order_id"
    )
)
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Don hang duoc thay doi trang thai
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Trang thai truoc khi cap nhat
    @Column(name = "from_status", length = 30)
    private String fromStatus;

    // Trang thai sau khi cap nhat
    @Column(
        name = "to_status",
        nullable = false,
        length = 30
    )
    private String toStatus;

    // Email Admin thuc hien thay doi
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    // Thoi gian thay doi
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public OrderStatusHistory(
            Order order,
            String fromStatus,
            String toStatus,
            String changedBy) {

        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
    }

    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
