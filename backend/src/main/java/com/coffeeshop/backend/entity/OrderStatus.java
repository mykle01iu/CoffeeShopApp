
package com.coffeeshop.backend.entity;

public enum OrderStatus {

    PENDING,    // chờ xác nhận
    PREPARING,  // đang chuẩn bị
    COMPLETED,  // đã hoàn thành
    CANCELLED;  // đã hủy

    //Kiểm tra xem trạng thái hiện tại có thể chuyển sang trạng thái tiếp theo hay không
    public boolean canTransitionTo(OrderStatus next) {

        if (next == null) {
            return false;
        }

        return switch (this) {

            case PENDING ->
                    next == PREPARING
                    || next == CANCELLED;

            case PREPARING ->
                    next == COMPLETED
                    || next == CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };
    }
}
