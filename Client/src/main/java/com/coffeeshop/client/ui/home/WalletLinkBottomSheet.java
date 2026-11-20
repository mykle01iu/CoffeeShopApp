package com.coffeeshop.client.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.payment.PaymentRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class WalletLinkBottomSheet extends BottomSheetDialogFragment {
    public interface OnWalletLinkedCallback {
        void onWalletLinked();
    }

    private OnWalletLinkedCallback callback;
    private ChipGroup chipGroupWallet;
    private TextInputLayout tilWalletPhone;
    private EditText edtWalletPhone;
    private Button btnSubmitWalletLink;
    private ProgressBar progressWalletLink;

    public void setCallback(OnWalletLinkedCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_wallet_link, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupWallet = view.findViewById(R.id.chipGroupWallet);
        tilWalletPhone = view.findViewById(R.id.tilWalletPhone);
        edtWalletPhone = view.findViewById(R.id.edtWalletPhone);
        btnSubmitWalletLink = view.findViewById(R.id.btnSubmitWalletLink);
        progressWalletLink = view.findViewById(R.id.progressWalletLink);

        edtWalletPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnSubmitWalletLink.setOnClickListener(v -> submitWalletLink());
    }

    private boolean validatePhone() {
        String phone = edtWalletPhone.getText() != null ? edtWalletPhone.getText().toString().trim() : "";
        boolean isValid = phone.matches("^0\\d{9}$");
        if (phone.length() > 0 && !isValid) {
            tilWalletPhone.setError(getString(R.string.wallet_phone_error));
        } else {
            tilWalletPhone.setError(null);
        }
        btnSubmitWalletLink.setEnabled(isValid);
        btnSubmitWalletLink.setAlpha(isValid ? 1.0f : 0.5f);
        return isValid;
    }

    private void submitWalletLink() {
        if (!validatePhone()) return;

        String phone = edtWalletPhone.getText().toString().trim();
        int checkedChipId = chipGroupWallet.getCheckedChipId();
        String provider = (checkedChipId == R.id.chipZaloPay) ? "ZaloPay" : "MoMo";

        btnSubmitWalletLink.setEnabled(false);
        progressWalletLink.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PaymentRepository.getInstance().addWallet(provider, phone);
            progressWalletLink.setVisibility(View.GONE);

            if (getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        getString(R.string.wallet_linked_toast), Snackbar.LENGTH_SHORT).show();
            }

            if (callback != null) {
                callback.onWalletLinked();
            }
            dismiss();
        }, 1000L);
    }
}
