
package com.coffeeshop.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Don hang chua san pham nay
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ID san pham luc dat hang
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Ten san pham tai thoi diem mua
    @Column(name = "product_name", nullable = false)
    private String productName;

    // Gia san pham tai thoi diem mua
    @Column(
        name = "unit_price",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal unitPrice;

    // So luong
    @Column(nullable = false)
    private Integer quantity;

    // Thanh tien = don gia * so luong
    @Column(
        name = "line_total",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal lineTotal;

    public OrderDetail(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;

        this.lineTotal = unitPrice.multiply(
            BigDecimal.valueOf(quantity)
        );
    }
}
