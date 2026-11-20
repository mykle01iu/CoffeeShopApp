package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.data.VoucherRepository;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.CreateOrderRequestDto;
import com.coffeeshop.client.data.remote.dto.OrderResponseDto;
import com.coffeeshop.client.model.CartItem;
import com.coffeeshop.client.data.payment.PaymentMethod;
import com.coffeeshop.client.data.payment.PaymentRepository;
import com.coffeeshop.client.data.payment.PaymentType;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherType;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private TextView addressText;
    private TextView voucherText;
    private RadioGroup paymentGroup;
    private LinearLayout checkoutUnlinkedWalletsContainer;
    private EditText noteInput;

    private TextView subtotalText;
    private TextView shippingText;
    private TextView discountText;
    private TextView totalText;
    private Button btnPlaceOrder;

    private Address deliveryInfo;
    private Voucher selectedVoucher;
    private final Map<Integer, PaymentMethod> radioIdToMethodMap = new HashMap<>();
    private String selectedMethodId = "cod";
    private OrderViewModel orderViewModel;
    private boolean submittingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView title = findViewById(R.id.checkoutTitle);
        if (title != null) {
            title.setTypeface(FontUtils.lora(this, 600));
        }

        findViewById(R.id.checkoutBack).setOnClickListener(v -> finish());

        addressText = findViewById(R.id.checkoutAddressText);
        View card = (View) addressText.getParent().getParent();
        card.setClickable(true);
        card.setOnClickListener(v -> changeDeliveryInfo());
        deliveryInfo = savedInstanceState == null ? AddressRepository.get(this).getDefault()
                : (Address) savedInstanceState.getSerializable("checkout.delivery");
        addressText.setOnClickListener(v -> changeDeliveryInfo());
        voucherText = findViewById(R.id.checkoutVoucherText);
        paymentGroup = findViewById(R.id.paymentMethodGroup);
        checkoutUnlinkedWalletsContainer = findViewById(R.id.checkoutUnlinkedWalletsContainer);
        noteInput = findViewById(R.id.checkoutNoteInput);

        subtotalText = findViewById(R.id.summarySubtotalText);
        shippingText = findViewById(R.id.summaryShippingText);
        discountText = findViewById(R.id.summaryDiscountText);
        totalText = findViewById(R.id.summaryTotalText);

        findViewById(R.id.checkoutVoucherRow).setOnClickListener(v -> openVoucherPicker());

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> performPlaceOrder());

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getCreateOrderResult().observe(this, this::onCreateOrderResult);

        checkPreselectedVoucher();
        setupPaymentMethodsListener();
    }

    private void setupPaymentMethodsListener() {
        PaymentRepository.getInstance().getMethodsLiveData().observe(this, methods -> {
            if (methods != null) {
                renderPaymentMethods(methods);
            }
        });
    }

    private void renderPaymentMethods(List<PaymentMethod> methods) {
        if (paymentGroup == null) return;
        paymentGroup.removeAllViews();
        radioIdToMethodMap.clear();

        boolean hasWallet = false;
        boolean hasCard = false;
        int radioIdCounter = 1000;
        int defaultCheckId = -1;

        for (PaymentMethod method : methods) {
            if (method.getType() == PaymentType.WALLET) hasWallet = true;
            if (method.getType() == PaymentType.CARD) hasCard = true;

            int radioId = radioIdCounter++;
            RadioButton rb = new RadioButton(this);
            rb.setId(radioId);
            rb.setButtonTintList(getColorStateList(R.color.coffee_accent));

            String label = method.getDisplayName();
            if (method.getDetail() != null && !method.getDetail().isEmpty()) {
                label += " (" + method.getDetail() + ")";
            }
            rb.setText(label);
            rb.setTextColor(getColor(R.color.coffee_text));
            rb.setTextSize(14);
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    dp(44)
            ));

            radioIdToMethodMap.put(radioId, method);
            paymentGroup.addView(rb);

            if (method.isDefault() || (selectedMethodId != null && selectedMethodId.equals(method.getId()))) {
                defaultCheckId = radioId;
                selectedMethodId = method.getId();
            }
        }

        if (defaultCheckId != -1) {
            paymentGroup.check(defaultCheckId);
        } else if (paymentGroup.getChildCount() > 0) {
            paymentGroup.check(paymentGroup.getChildAt(0).getId());
        }

        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            PaymentMethod selected = radioIdToMethodMap.get(checkedId);
            if (selected != null) {
                selectedMethodId = selected.getId();
            }
        });

        // Unlinked Wallet / Card rows with "Liên kết ngay" button
        if (checkoutUnlinkedWalletsContainer != null) {
            checkoutUnlinkedWalletsContainer.removeAllViews();

            if (!hasWallet) {
                addUnlinkedRow(checkoutUnlinkedWalletsContainer, "Ví điện tử (Chưa liên kết)", () -> {
                    WalletLinkBottomSheet sheet = new WalletLinkBottomSheet();
                    sheet.show(getSupportFragmentManager(), "WalletLinkBottomSheet");
                });
            }

            if (!hasCard) {
                addUnlinkedRow(checkoutUnlinkedWalletsContainer, "Thẻ ngân hàng (Chưa liên kết)", () -> {
                    CardAddBottomSheet sheet = new CardAddBottomSheet();
                    sheet.show(getSupportFragmentManager(), "CardAddBottomSheet");
                });
            }
        }
    }

    private void addUnlinkedRow(LinearLayout container, String titleText, Runnable onLinkAction) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
        ));
        row.setAlpha(0.6f);

        TextView label = new TextView(this);
        label.setText(titleText);
        label.setTextColor(getColor(R.color.coffee_muted));
        label.setTextSize(13);
        LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        lpLabel.setMarginStart(dp(28));
        label.setLayoutParams(lpLabel);

        TextView btnLink = new TextView(this);
        btnLink.setText(R.string.checkout_link_now);
        btnLink.setTextColor(getColor(R.color.coffee_brown));
        btnLink.setTextSize(13);
        btnLink.setTypeface(FontUtils.inter(this, 600));
        btnLink.setOnClickListener(v -> onLinkAction.run());

        row.addView(label);
        row.addView(btnLink);
        container.addView(row);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deliveryInfo == null) {
            deliveryInfo = AddressRepository.get(this).getDefault();
        }
        renderDelivery();
        calculateSummary();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (deliveryInfo != null) {
            outState.putSerializable("checkout.delivery", deliveryInfo);
        }
    }

    private void renderDelivery() {
        TextView warning = findViewById(R.id.checkoutAddressWarning);
        if (deliveryInfo != null) {
            addressText.setText(getString(R.string.address_home_delivery_prefix, deliveryInfo.street, deliveryInfo.district));
            addressText.setTextColor(getColor(R.color.coffee_text));
            if (warning != null) warning.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setAlpha(1.0f);
        } else {
            addressText.setText(R.string.home_address_description);
            addressText.setTextColor(getColor(R.color.coffee_muted));
            if (warning != null) warning.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(false);
            btnPlaceOrder.setAlpha(0.5f);
        }
    }

    private void changeDeliveryInfo() {
        AddressDialogs.showPicker(this, deliveryInfo, selected -> {
            deliveryInfo = selected;
            renderDelivery();
        });
    }

    private void checkPreselectedVoucher() {
        String code = VoucherRepository.getSelectedVoucherCode(this);
        if (code != null) {
            selectedVoucher = VoucherRepository.getVoucherByCode(code);
        }
    }

    private void openVoucherPicker() {
        int subtotal = CartRepository.subtotal(this);
        VoucherPickerBottomSheet picker = new VoucherPickerBottomSheet(subtotal, voucher -> {
            selectedVoucher = voucher;
            VoucherRepository.setSelectedVoucherCode(this, voucher.code);
            calculateSummary();
        });
        picker.show(getSupportFragmentManager(), "VoucherPickerBottomSheet");
    }

    private void calculateSummary() {
        int subtotal = CartRepository.subtotal(this);
        int shipping = 15000;
        int discount = VoucherRepository.calculateDiscount(selectedVoucher, subtotal, shipping);
        if (selectedVoucher != null && selectedVoucher.type == VoucherType.FREESHIP) {
            shipping = 0;
        }
        int total = Math.max(0, subtotal + shipping - discount);

        NumberFormat fmt = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

        if (selectedVoucher != null) {
            voucherText.setText(selectedVoucher.code + " (-" + fmt.format(discount) + "đ) ✖");
            voucherText.setOnClickListener(v -> {
                selectedVoucher = null;
                VoucherRepository.clearSelectedVoucherCode(this);
                calculateSummary();
                findViewById(R.id.checkoutVoucherRow).setOnClickListener(view -> openVoucherPicker());
            });
        } else {
            voucherText.setText("Chọn hoặc nhập mã ›");
            findViewById(R.id.checkoutVoucherRow).setOnClickListener(v -> openVoucherPicker());
        }

        subtotalText.setText(fmt.format(subtotal) + "đ");
        shippingText.setText(fmt.format(shipping) + "đ");
        discountText.setText("-" + fmt.format(discount) + "đ");
        totalText.setText(fmt.format(total) + "đ");
    }

    private void performPlaceOrder() {
        if (submittingOrder) return;
        if (deliveryInfo == null) {
            Toast.makeText(this, R.string.delivery_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartItems = CartRepository.getItems(this);
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }
        List<CreateOrderRequestDto.Item> requestItems = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            requestItems.add(new CreateOrderRequestDto.Item((long) item.product.id, item.quantity));
        }
        submittingOrder = true;
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang đặt hàng...");
        orderViewModel.createOrder(requestItems);
    }

    private void onCreateOrderResult(ApiResult<OrderResponseDto> result) {
        if (result == null || result.status == ApiResult.Status.LOADING) return;
        submittingOrder = false;
        btnPlaceOrder.setEnabled(deliveryInfo != null);
        btnPlaceOrder.setText(R.string.checkout_place_order_button);
        if (result.status == ApiResult.Status.ERROR || result.data == null) {
            Toast.makeText(this, result.message == null ? "Không tạo được đơn hàng" : result.message,
                    Toast.LENGTH_LONG).show();
            return;
        }

        com.coffeeshop.client.model.Order order = OrderRepository.cacheRemoteOrder(result.data);
        if (order == null) {
            Toast.makeText(this, "Máy chủ trả về đơn hàng không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }
        CartRepository.clear(this);
        VoucherRepository.clearSelectedVoucherCode(this);
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("orderId", order.id);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
