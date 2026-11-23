package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.OrderStatusHistoryResponse;
import com.coffeeshop.backend.dto.UpdateOrderStatusRequest;

import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderStatus;
import com.coffeeshop.backend.entity.OrderStatusHistory;

import com.coffeeshop.backend.repository.OrderRepository;
import com.coffeeshop.backend.repository.OrderStatusHistoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderStatusService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository) {

        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    // 1. ADMIN cap nhat trang thai don hang
    @Transactional
    public OrderStatusHistoryResponse updateStatus(
            Long orderId,
            UpdateOrderStatusRequest request,
            Authentication authentication) {

        // Kiem tra quyen ADMIN
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chi ADMIN duoc cap nhat trang thai"
            );
        }

        // Kiem tra du lieu gui len
        if (request == null || request.status() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trang thai khong hop le"
            );
        }

        // Tim va khoa don hang khi cap nhat
        Order order = orderRepository
                .findByIdForStatusUpdate(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay don hang"
                        )
                );

        // Lay trang thai hien tai
        OrderStatus currentStatus;

        try {
            currentStatus = OrderStatus.valueOf(
                    order.getStatus()
            );
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Trang thai hien tai khong hop le"
            );
        }

        // Trang thai ADMIN muon chuyen sang
        OrderStatus newStatus = request.status();

        // Kiem tra quy tac chuyen trang thai
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khong the chuyen tu "
                            + currentStatus
                            + " sang "
                            + newStatus
            );
        }

        // Cap nhat trang thai moi
        order.setStatus(newStatus.name());

        // Tao ban ghi lich su
        OrderStatusHistory history =
                new OrderStatusHistory(
                        order,
                        currentStatus.name(),
                        newStatus.name(),
                        authentication.getName()
                );

        // Luu lich su trong cung transaction
        OrderStatusHistory savedHistory =
                historyRepository.saveAndFlush(history);

        return toResponse(savedHistory);
    }

    // 2. Lay lich su thay doi trang thai
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistory(
            Long orderId,
            Authentication authentication) {

        // Yeu cau dang nhap
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Can dang nhap"
            );
        }

        // Tim don hang
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay don hang"
                        )
                );

        // Kiem tra nguoi so huu don hang
        boolean isOwner = order.getUser()
                .getEmail()
                .equalsIgnoreCase(authentication.getName());

        // ADMIN xem moi don
        // USER chi xem don cua minh
        if (!isAdmin(authentication) && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Khong co quyen xem lich su don hang"
            );
        }

        // Lay lich su tu MySQL
        return historyRepository
                .findByOrder_IdOrderByChangedAtAscIdAsc(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 3. Kiem tra quyen ADMIN
    private boolean isAdmin(Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );
    }

    // 4. Chuyen Entity thanh Response DTO
    private OrderStatusHistoryResponse toResponse(
            OrderStatusHistory history) {

        return new OrderStatusHistoryResponse(
                history.getId(),
                history.getOrder().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy(),
                history.getChangedAt()
        );
    }
}
