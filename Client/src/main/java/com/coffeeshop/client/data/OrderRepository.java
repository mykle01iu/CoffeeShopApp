package com.coffeeshop.client.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.data.remote.ApiExecutor;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.ApiService;
import com.coffeeshop.client.data.remote.RetrofitClient;
import com.coffeeshop.client.data.remote.dto.CreateOrderRequestDto;
import com.coffeeshop.client.data.remote.dto.OrderResponseDto;
import com.coffeeshop.client.data.payment.PaymentType;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class OrderRepository {
    private static int orderCounter = 82122;
    private static final List<Order> ORDER_LIST = new ArrayList<>();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private final ApiService api;

    public OrderRepository(Context context) {
        api = RetrofitClient.getApiService(context.getApplicationContext());
    }

    public ApiResult<OrderResponseDto> createRemoteOrder(CreateOrderRequestDto request) {
        return ApiExecutor.safeApiCall(api.createOrder(request));
    }

    public ApiResult<List<OrderResponseDto>> fetchRemoteOrders() {
        return ApiExecutor.safeApiCall(api.getOrders());
    }

    public static synchronized Order cacheRemoteOrder(OrderResponseDto response) {
        Order order = fromRemote(response);
        if (order == null) return null;
        removeCachedOrder(order.id, false);
        ORDER_LIST.add(0, order);
        return order;
    }

    public static synchronized List<Order> cacheRemoteOrders(List<OrderResponseDto> responses) {
        List<Order> orders = new ArrayList<>();
        if (responses != null) for (OrderResponseDto response : responses) {
            Order order = fromRemote(response);
            if (order != null) orders.add(order);
        }
        removeCachedOrder(null, true);
        for (int i = orders.size() - 1; i >= 0; i--) ORDER_LIST.add(0, orders.get(i));
        return Collections.unmodifiableList(orders);
    }

    private static void removeCachedOrder(String id, boolean remoteOnly) {
        Iterator<Order> iterator = ORDER_LIST.iterator();
        while (iterator.hasNext()) {
            Order current = iterator.next();
            if ((id != null && current.id.equals(id)) || (remoteOnly && current.remoteOrder)) iterator.remove();
        }
    }

    private static Order fromRemote(OrderResponseDto response) {
        if (response == null || response.id == null) return null;
        List<CartItem> items = new ArrayList<>();
        if (response.items != null) for (OrderResponseDto.Item item : response.items) {
            if (item == null || item.productId == null || item.quantity == null || item.unitPrice == null
                    || item.productId < 1 || item.productId > Integer.MAX_VALUE) continue;
            Product product = new Product(item.productId.intValue(),
                    item.productName == null ? "" : item.productName, "", item.unitPrice.intValue(),
                    0f, 0, "", R.drawable.logo_moc_mien_icon);
            com.coffeeshop.client.model.SelectedOptions noOptions = new com.coffeeshop.client.model.SelectedOptions(
                    null, null, null, Collections.emptyList(), false, "");
            items.add(new CartItem(product, noOptions, item.quantity));
        }
        int total = response.totalAmount == null ? 0 : response.totalAmount.intValue();
        OrderStatus status;
        try { status = OrderStatus.valueOf(response.status == null ? "PENDING" : response.status); }
        catch (IllegalArgumentException ignored) { status = OrderStatus.PENDING; }
        String orderId = "#" + String.format(Locale.getDefault(), "%06d", response.id);
        return new Order(orderId, items, total, 0, 0, total, null,
                PaymentMethod.CASH, "", "", "", "", "", "", response.createdAt,
                status, 0, true);
    }

    public static synchronized List<Order> getOrders(Context context) {
        return Collections.unmodifiableList(new ArrayList<>(ORDER_LIST));
    }

    public static synchronized Order placeOrder(Context context, com.coffeeshop.client.data.payment.PaymentMethod selectedPayment, Address delivery, String note, String voucherCode) {
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
        int pointsEarned = total / 10000;
        String orderId = "#" + String.format(Locale.getDefault(), "%06d", orderCounter++);
        String dateStr = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());

        PaymentMethod legacyMethod = PaymentMethod.CASH;
        String displayName = "Tiền mặt khi nhận hàng";
        String detail = "";
        if (selectedPayment != null) {
            displayName = selectedPayment.getDisplayName();
            detail = selectedPayment.getDetail();
            if (selectedPayment.getType() == PaymentType.WALLET) {
                legacyMethod = PaymentMethod.WALLET;
            } else if (selectedPayment.getType() == PaymentType.CARD) {
                legacyMethod = PaymentMethod.CARD;
            }
        }

        Order newOrder = new Order(
                orderId, cartItems, subtotal, shippingFee, discount, total, voucherCode,
                legacyMethod, displayName, detail,
                delivery.receiverName, delivery.phone, delivery.getFullAddress(),
                note != null ? note : "", dateStr, OrderStatus.PREPARING, pointsEarned
        );

        ORDER_LIST.add(0, newOrder);
        CartRepository.clear(context);

        MAIN_HANDLER.postDelayed(() -> completeOrder(newOrder.id), 15000L);
        return newOrder;
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
