package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.data.VoucherRepository;
import com.coffeeshop.client.model.Order;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.model.PaymentMethod;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherType;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextView addressText, voucherText, subtotalText, shippingText, discountText, totalText;
    private Address deliveryInfo;
    private Button btnPlaceOrder;
    private RadioGroup paymentGroup;
    private RadioButton radioCash, radioWallet, radioCard;
    private EditText noteInput;

    private Voucher selectedVoucher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.checkoutTitle)).setTypeface(FontUtils.lora(this, 600));

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
        radioCash = findViewById(R.id.radioCash);
        radioWallet = findViewById(R.id.radioWallet);
        radioCard = findViewById(R.id.radioCard);
        noteInput = findViewById(R.id.checkoutNoteInput);

        subtotalText = findViewById(R.id.summarySubtotalText);
        shippingText = findViewById(R.id.summaryShippingText);
        discountText = findViewById(R.id.summaryDiscountText);
        totalText = findViewById(R.id.summaryTotalText);

        findViewById(R.id.checkoutVoucherRow).setOnClickListener(v -> openVoucherPicker());

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> performPlaceOrder());

        checkPreselectedVoucher();
        calculateSummary();
        renderDeliveryInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPreselectedVoucher();
        calculateSummary();
        renderDeliveryInfo();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (deliveryInfo != null) out.putSerializable("checkout.delivery", deliveryInfo);
        super.onSaveInstanceState(out);
    }

    private void changeDeliveryInfo() {
        if (AddressRepository.get(this).getAddresses().isEmpty()) {
            AddressDialogs.showForm(this, null, a -> { deliveryInfo=a; renderDeliveryInfo(); });
        } else {
            AddressDialogs.showPicker(this, deliveryInfo, a -> { deliveryInfo=a; renderDeliveryInfo(); });
        }
    }

    private void renderDeliveryInfo() {
        boolean ready=deliveryInfo!=null;
        btnPlaceOrder.setEnabled(ready);
        btnPlaceOrder.setAlpha(ready?1f:.55f);
        TextView warning=findViewById(R.id.checkoutAddressWarning);
        if(warning!=null) warning.setVisibility(ready?View.GONE:View.VISIBLE);
        if (ready) {
            findViewById(R.id.checkoutAddressCard).setBackgroundResource(R.drawable.bg_product_card);
            ((android.widget.ImageView)findViewById(R.id.checkoutAddressIcon)).setImageResource(R.drawable.ic_location);
            addressText.setText(deliveryInfo.receiverName+"  ·  "+deliveryInfo.phone+"\n"+deliveryInfo.getFullAddress()+"\n"+
                    getString(deliveryInfo.label==Address.AddressLabel.HOME?R.string.address_home_label:deliveryInfo.label==Address.AddressLabel.OFFICE?R.string.address_office_label:R.string.address_other_label)+
                    (isSavedAddress(deliveryInfo)?"":"  ·  "+getString(R.string.delivery_one_time))+"  ·  "+getString(R.string.delivery_change));
        } else {
            findViewById(R.id.checkoutAddressCard).setBackgroundResource(R.drawable.bg_address_empty);
            ((android.widget.ImageView)findViewById(R.id.checkoutAddressIcon)).setImageResource(android.R.drawable.ic_input_add);
            addressText.setText(R.string.delivery_enter);
        }
    }

    private boolean isSavedAddress(Address address) {
        for (Address saved : AddressRepository.get(this).getAddresses()) if (saved.id.equals(address.id)) return true;
        return false;
    }

    private void checkPreselectedVoucher() {
        if (selectedVoucher != null) return;
        String preselectedCode = VoucherRepository.getSelectedVoucherCode(this);
        if (preselectedCode != null && !preselectedCode.isEmpty()) {
            Voucher v = VoucherRepository.getVoucherByCode(preselectedCode);
            int subtotal = CartRepository.subtotal(this);
            if (v != null && VoucherRepository.validate(this, v.code, subtotal) == null) {
                selectedVoucher = v;
            }
        }
    }

    private void openVoucherPicker() {
        int subtotal = CartRepository.subtotal(this);
        VoucherPickerBottomSheet picker = new VoucherPickerBottomSheet(subtotal, voucher -> {
            selectedVoucher = voucher;
            if (voucher != null) {
                VoucherRepository.setSelectedVoucherCode(this, voucher.code);
            }
            calculateSummary();
        });
        picker.show(getSupportFragmentManager(), "VoucherPickerBottomSheet");
    }

    private void calculateSummary() {
        int subtotal = CartRepository.subtotal(this);
        int shipping = 15000;

        if (selectedVoucher != null) {
            String validationError = VoucherRepository.validate(this, selectedVoucher.code, subtotal);
            if (validationError != null) {
                Toast.makeText(this, "Mã " + selectedVoucher.code + " không còn áp dụng được cho đơn này", Toast.LENGTH_LONG).show();
                selectedVoucher = null;
                VoucherRepository.clearSelectedVoucherCode(this);
            }
        }

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
        PaymentMethod method = PaymentMethod.CASH;
        int checkedId = paymentGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioWallet) {
            method = PaymentMethod.WALLET;
        } else if (checkedId == R.id.radioCard) {
            method = PaymentMethod.CARD;
        }

        if (deliveryInfo == null) return;
        String note = noteInput.getText().toString().trim();
        String voucherCode = selectedVoucher != null ? selectedVoucher.code : null;

        Order order = OrderRepository.placeOrder(this, method, deliveryInfo, note, voucherCode);

        if (order != null) {
            if (selectedVoucher != null) {
                String todayStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                VoucherRepository.markUsed(this, selectedVoucher.code, todayStr);
            }

            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.putExtra("orderId", order.id);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
