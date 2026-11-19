package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.model.CartItem;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.model.OrderStatus;
import com.coffeeshop.client.model.PaymentMethod;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends Activity {

    private String orderId;
    private TextView codeText, dateText, statusChip, addressText, paymentText;
    private TextView subtotalText, shippingText, discountLabel, discountText, totalText, pointsEarnedText;
    private LinearLayout itemsContainer;
    private Button btnCancelOrder, btnReorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        orderId = getIntent().getStringExtra("orderId");

        ((TextView) findViewById(R.id.detailTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.detailBack).setOnClickListener(v -> finish());

        codeText = findViewById(R.id.detailCodeText);
        dateText = findViewById(R.id.detailDateText);
        statusChip = findViewById(R.id.detailStatusChip);

        itemsContainer = findViewById(R.id.detailItemsContainer);

        addressText = findViewById(R.id.detailAddressText);
        paymentText = findViewById(R.id.detailPaymentText);

        subtotalText = findViewById(R.id.detailSubtotalText);
        shippingText = findViewById(R.id.detailShippingText);
        discountLabel = findViewById(R.id.detailDiscountLabel);
        discountText = findViewById(R.id.detailDiscountText);
        totalText = findViewById(R.id.detailTotalText);
        pointsEarnedText = findViewById(R.id.detailPointsEarnedText);

        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnReorder = findViewById(R.id.btnReorder);

        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());

        loadOrderDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetail();
    }

    private void loadOrderDetail() {
        Order order = OrderRepository.getOrderById(orderId);
        if (order == null) {
            Toast.makeText(this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        codeText.setText(order.id);
        dateText.setText(order.createdAt);
        addressText.setText(order.receiverName + " · " + order.phone + "\n" + order.address);

        String methodStr = "Tiền mặt khi nhận hàng (COD)";
        if (order.paymentMethod == PaymentMethod.WALLET) {
            methodStr = "Ví điện tử MoMo / ZaloPay";
        } else if (order.paymentMethod == PaymentMethod.CARD) {
            methodStr = "Thẻ ngân hàng (ATM / Credit)";
        }
        paymentText.setText("Thanh toán: " + methodStr);

        if (order.status == OrderStatus.PREPARING) {
            statusChip.setText("ĐANG CHUẨN BỊ");
            statusChip.setBackgroundColor(Color.parseColor("#FFF3E0"));
            statusChip.setTextColor(Color.parseColor("#EF6C00"));
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else if (order.status == OrderStatus.COMPLETED) {
            statusChip.setText("HOÀN THÀNH");
            statusChip.setBackgroundColor(Color.parseColor("#E8F5E9"));
            statusChip.setTextColor(Color.parseColor("#2E7D32"));
            btnCancelOrder.setVisibility(View.GONE);
        } else {
            statusChip.setText("ĐÃ HỦY");
            statusChip.setBackgroundColor(Color.parseColor("#F5F5F5"));
            statusChip.setTextColor(Color.parseColor("#757575"));
            btnCancelOrder.setVisibility(View.GONE);
        }

        itemsContainer.removeAllViews();
        NumberFormat fmt = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

        if (order.items != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (CartItem item : order.items) {
                View itemView = inflater.inflate(R.layout.item_cart_line, itemsContainer, false);
                ((TextView) itemView.findViewById(R.id.cartItemName)).setText(item.product.name);
                ((TextView) itemView.findViewById(R.id.cartItemPrice)).setText(getString(R.string.money_vnd, fmt.format(item.getLineTotal())));
                TextView optionSummary = itemView.findViewById(R.id.cartItemOptions); optionSummary.setText(item.getOptionSummary(this)); optionSummary.setVisibility(optionSummary.getText().length() == 0 ? View.GONE : View.VISIBLE);
                TextView optionNote = itemView.findViewById(R.id.cartItemNote); optionNote.setText(getString(R.string.cart_note_format, item.options.note)); optionNote.setVisibility(item.options.note.isEmpty() ? View.GONE : View.VISIBLE);
                ((TextView) itemView.findViewById(R.id.cartItemQty)).setText("x" + item.quantity);
                ((ImageView) itemView.findViewById(R.id.cartItemImage)).setImageResource(item.product.imageRes);

                itemView.findViewById(R.id.btnCartMinus).setVisibility(View.GONE);
                itemView.findViewById(R.id.btnCartPlus).setVisibility(View.GONE);
                itemView.findViewById(R.id.btnRemoveCartItem).setVisibility(View.GONE);

                itemsContainer.addView(itemView);
            }
        }

        subtotalText.setText(fmt.format(order.subtotal) + "đ");
        shippingText.setText(fmt.format(order.shippingFee) + "đ");

        if (order.voucherCode != null && !order.voucherCode.isEmpty()) {
            discountLabel.setText("Giảm giá (" + order.voucherCode + ")");
        } else {
            discountLabel.setText("Giảm giá");
        }
        discountText.setText("-" + fmt.format(order.discount) + "đ");

        totalText.setText(fmt.format(order.total) + "đ");

        if (order.status == OrderStatus.COMPLETED && order.pointsEarned > 0) {
            pointsEarnedText.setText("Tích lũy được +" + order.pointsEarned + " điểm");
            pointsEarnedText.setVisibility(View.VISIBLE);
        } else {
            pointsEarnedText.setVisibility(View.GONE);
        }

        btnReorder.setOnClickListener(v -> {
            OrderRepository.reorder(this, order);
            Toast.makeText(this, "Đã thêm các món vào giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng " + orderId + " không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    boolean cancelled = OrderRepository.cancelOrder(this, orderId);
                    if (cancelled) {
                        Toast.makeText(this, "Đã hủy đơn hàng " + orderId, Toast.LENGTH_SHORT).show();
                        loadOrderDetail();
                    } else {
                        Toast.makeText(this, "Không thể hủy đơn hàng này", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
