
package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.CreateOrderRequest;
import com.coffeeshop.backend.dto.OrderResponse;

import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderDetail;
import com.coffeeshop.backend.entity.OrderStatusHistory;
import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.entity.User;

import com.coffeeshop.backend.repository.OrderRepository;
import com.coffeeshop.backend.repository.OrderStatusHistoryRepository;
import com.coffeeshop.backend.repository.ProductRepository;
import com.coffeeshop.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderStatusHistoryRepository historyRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    // 1. Tao don hang
    @Transactional
    public OrderResponse createOrder(
            String email,
            CreateOrderRequest request) {

        // Tim nguoi dung dang dang nhap
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Nguoi dung khong ton tai"
                        )
                );

        // Kiem tra danh sach san pham
        if (request == null
                || request.items() == null
                || request.items().isEmpty()
                || request.items().size() > 50) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Danh sach san pham khong hop le"
            );
        }

        Order order = new Order(user);

        Set<Long> productIds = new HashSet<>();

        for (CreateOrderRequest.Item item : request.items()) {

            // Kiem tra input
            if (item == null
                    || item.productId() == null
                    || item.productId() <= 0
                    || item.quantity() == null
                    || item.quantity() < 1
                    || item.quantity() > 100) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "San pham hoac so luong khong hop le"
                );
            }

            // Khong cho trung productId
            if (!productIds.add(item.productId())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "San pham bi trung trong don"
                );
            }

            // Lay Product tu MySQL
            Product product = productRepository
                    .findById(item.productId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Khong tim thay san pham"
                            )
                    );

            // Kiem tra san pham con ban
            if (!Boolean.TRUE.equals(
                    product.getAvailable())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "San pham dang ngung ban"
                );
            }

            // Gia phai lay tu database
            BigDecimal price = product.getPrice();

            if (price == null
                    || price.signum() <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Gia san pham khong hop le"
                );
            }

            try {
                price = price.setScale(
                        2,
                        RoundingMode.UNNECESSARY
                );
            } catch (ArithmeticException ex) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Gia san pham co qua 2 so thap phan"
                );
            }

            // Tao chi tiet voi gia that
            OrderDetail detail = new OrderDetail(
                    product.getId(),
                    product.getName(),
                    price,
                    item.quantity()
            );

            // Them chi tiet va cong tong tien
            order.addDetail(detail);
        }

        // Luu Order va OrderDetail trong 1 transaction
        Order savedOrder =
                orderRepository.saveAndFlush(order);

        // NGAY 7:
        // Ghi lich su trang thai ban dau cua don hang
        OrderStatusHistory history =
                new OrderStatusHistory(
                        savedOrder,
                        null,
                        savedOrder.getStatus(),
                        email
                );

        historyRepository.save(history);

        return toResponse(savedOrder);
    }

    // 2. Lay don hang cua user hien tai
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Nguoi dung khong ton tai"
                        )
                );

        return orderRepository
                .findByUser_IdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 3. Lay tat ca don hang cho Admin
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 4. Chuyen Entity thanh DTO
    private OrderResponse toResponse(Order order) {

        List<OrderResponse.ItemResponse> items =
                order.getDetails()
                        .stream()
                        .map(detail ->
                                new OrderResponse.ItemResponse(
                                        detail.getProductId(),
                                        detail.getProductName(),
                                        detail.getUnitPrice(),
                                        detail.getQuantity(),
                                        detail.getLineTotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}
