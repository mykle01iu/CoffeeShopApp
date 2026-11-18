package com.example.myapplication.data;

import com.example.myapplication.model.Order;

import java.util.Arrays;
import java.util.List;

/** Local sample orders; this feature deliberately does not call the backend. */
public final class MockOrders {
    private MockOrders() {}

    public static List<Order> all() {
        return Arrays.asList(
                new Order("DH-20261118-024", "Nguyễn Minh Anh", "0901 234 567", "18/11/2026 · 10:42",
                        "Chờ xác nhận", "Giao tận nơi", "12 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",
                        "Thanh toán khi nhận hàng", "Ít đá, ít ngọt", 15000,
                        new Order.Item("Cà phê sữa đá", 2, 45000), new Order.Item("Croissant bơ", 1, 35000)),
                new Order("DH-20261118-023", "Trần Quốc Bảo", "0912 345 678", "18/11/2026 · 10:18",
                        "Đang pha chế", "Đến lấy tại quán", "Mộc Miên Coffee · 25 Lê Lợi, Quận 1",
                        "Ví điện tử", "", 0,
                        new Order.Item("Latte caramel", 1, 59000), new Order.Item("Trà đào cam sả", 1, 52000)),
                new Order("DH-20261118-022", "Lê Thu Hà", "0987 654 321", "18/11/2026 · 09:56",
                        "Đang giao", "Giao tận nơi", "88 Điện Biên Phủ, Bình Thạnh, TP. Hồ Chí Minh",
                        "Đã thanh toán", "Gọi khi đến nơi", 15000,
                        new Order.Item("Bạc xỉu", 2, 47000)),
                new Order("DH-20261118-021", "Phạm Gia Huy", "0933 221 100", "18/11/2026 · 09:32",
                        "Hoàn thành", "Đến lấy tại quán", "Mộc Miên Coffee · 25 Lê Lợi, Quận 1",
                        "Tiền mặt", "", 0,
                        new Order.Item("Americano đá", 1, 42000), new Order.Item("Cookie chocolate", 2, 28000)),
                new Order("DH-20261118-020", "Đỗ Ngọc Mai", "0908 111 222", "18/11/2026 · 09:10",
                        "Đã hủy", "Giao tận nơi", "15 Võ Văn Tần, Quận 3, TP. Hồ Chí Minh",
                        "Chưa thanh toán", "Khách đổi kế hoạch", 15000,
                        new Order.Item("Trà vải hoa hồng", 1, 54000))
        );
    }
}
