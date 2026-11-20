package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.CreateOrderRequest;
import com.coffeeshop.backend.dto.OrderResponse;
import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderItem;
import com.coffeeshop.backend.entity.OrderStatus;
import com.coffeeshop.backend.entity.Product;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.repository.OrderRepository;
import com.coffeeshop.backend.repository.ProductRepository;
import com.coffeeshop.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(String authenticatedIdentifier, CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don hang phai co it nhat mot mon");
        }

        User user = findAuthenticatedUser(authenticatedIdentifier);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.Item requestedItem : request.items()) {
            if (requestedItem == null || requestedItem.productId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId khong hop le");
            }
            if (requestedItem.quantity() == null || requestedItem.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity phai lon hon 0");
            }

            Product product = productRepository.findById(requestedItem.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Khong tim thay san pham " + requestedItem.productId()));
            if (!Boolean.TRUE.equals(product.getAvailable())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "San pham ngung kinh doanh: " + product.getId());
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(requestedItem.quantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(requestedItem.quantity())));
        }

        order.setTotalAmount(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String authenticatedIdentifier) {
        User user = findAuthenticatedUser(authenticatedIdentifier);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderResponse::from)
                .toList();
    }

    private User findAuthenticatedUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Khong xac dinh duoc tai khoan");
        }
        return (identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhone(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Tai khoan xac thuc khong ton tai"));
    }
}
