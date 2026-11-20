package com.coffeeshop.client.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.coffeeshop.client.model.CartItem;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.model.OrderStatus;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.model.PaymentMethod;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class OrderRepository {
    private static int orderCounter = 82122;
    private static final List<Order> ORDER_LIST = new ArrayList<>();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private OrderRepository() { }

    public static synchronized List<Order> getOrders(Context context) {
        return Collections.unmodifiableList(new ArrayList<>(ORDER_LIST));
    }

    public static synchronized Order placeOrder(Context context, PaymentMethod paymentMethod, Address delivery, String note, String voucherCode) {
        if (context == null) return null;

        List<CartItem> cartItems = CartRepository.getItems(context);
        int subtotal = CartRepository.subtotal(context);
        int shippingFee = 15000;

        Voucher v = VoucherRepository.getVoucherByCode(voucherCode);
        int discount = VoucherRepository.calculateDiscount(v, subtotal, shippingFee);
        if (v != null && v.type == VoucherType.FREESHIP) {
            shippingFee = 0;
        }

        int total = Math.max(0, subtotal + shippingFee - discount);
        int pointsEarned = total / 10000; // 1 điểm cho mỗi 10.000đ
        String orderId = "#" + String.format(Locale.getDefault(), "%06d", orderCounter++);
        String dateStr = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Order newOrder = new Order(
                orderId,
                cartItems,
                subtotal,
                shippingFee,
                discount,
                total,
                voucherCode,
                paymentMethod != null ? paymentMethod : PaymentMethod.CASH,
                delivery.receiverName,
                delivery.phone,
                delivery.getFullAddress(),
                note != null ? note : "",
                dateStr,
                OrderStatus.PREPARING,
                pointsEarned
        );

        ORDER_LIST.add(0, newOrder);
        CartRepository.clear(context);

        // Giả lập: sau 15 giây tự chuyển sang COMPLETED và cộng điểm
        MAIN_HANDLER.postDelayed(() -> completeOrder(newOrder.id), 15000L);

        return newOrder;
    }

    private static synchronized void completeOrder(String orderId) {
        for (int i = 0; i < ORDER_LIST.size(); i++) {
            Order o = ORDER_LIST.get(i);
            if (o.id.equals(orderId) && o.status == OrderStatus.PREPARING) {
                Order updated = new Order(
                        o.id, o.items, o.subtotal, o.shippingFee, o.discount,
                        o.total, o.voucherCode, o.paymentMethod, o.receiverName, o.phone, o.address,
                        o.note, o.createdAt, OrderStatus.COMPLETED, o.pointsEarned
                );
                ORDER_LIST.set(i, updated);
                if (o.pointsEarned > 0) {
                    String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    PointsRepository.addTransaction("Đơn hàng " + o.id, o.pointsEarned, dateStr);
                }
                break;
            }
        }
    }

    public static synchronized boolean cancelOrder(Context context, String orderId) {
        if (orderId == null) return false;
        for (int i = 0; i < ORDER_LIST.size(); i++) {
            Order o = ORDER_LIST.get(i);
            if (o.id.equals(orderId) && o.status == OrderStatus.PREPARING) {
                Order cancelled = new Order(
                        o.id, o.items, o.subtotal, o.shippingFee, o.discount,
                        o.total, o.voucherCode, o.paymentMethod, o.receiverName, o.phone, o.address,
                        o.note, o.createdAt, OrderStatus.CANCELLED, 0
                );
                ORDER_LIST.set(i, cancelled);

                // Trả lại voucher nếu có
                if (o.voucherCode != null && !o.voucherCode.isEmpty()) {
                    VoucherRepository.restore(context, o.voucherCode);
                }
                return true;
            }
        }
        return false;
    }

    public static synchronized void reorder(Context context, Order order) {
        if (context == null || order == null || order.items == null) return;
        for (CartItem item : order.items) {
            CartRepository.add(context, item.product, item.options, item.quantity);
        }
    }

    public static synchronized Order getOrderById(String orderId) {
        if (orderId == null) return null;
        for (Order o : ORDER_LIST) {
            if (o.id.equals(orderId)) return o;
        }
        return null;
    }
}
