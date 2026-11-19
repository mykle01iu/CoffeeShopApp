
package com.coffeeshop.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "CoffeeOrder")
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nguoi dat hang
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Tong tien do Backend tinh
    @Column(
        name = "total_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Trang thai ban dau cua don
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    // Thoi gian tao don
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Danh sach san pham trong don
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderDetail> details = new ArrayList<>();

    public Order(User user) {
        this.user = user;
        this.totalAmount = BigDecimal.ZERO;
        this.status = "PENDING";
    }

    // Them chi tiet va cong don tong tien
    public void addDetail(OrderDetail detail) {
        detail.setOrder(this);
        details.add(detail);

        totalAmount = totalAmount.add(
            detail.getLineTotal()
        );
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "PENDING";
        }
    }
}
