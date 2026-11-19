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

public class LinkWalletBottomSheet extends BottomSheetDialogFragment {
    public interface OnWalletLinkedCallback {
        void onWalletLinked();
    }

    private OnWalletLinkedCallback callback;
    private ChipGroup chipGroupProvider;
    private TextInputLayout tilPhone;
    private EditText edtPhone;
    private ProgressBar progressBar;
    private Button btnSubmit;

    private String selectedProvider = "MoMo";
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LinkWalletBottomSheet() { }

    public LinkWalletBottomSheet(OnWalletLinkedCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_link_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupProvider = view.findViewById(R.id.chipGroupWalletProvider);
        tilPhone = view.findViewById(R.id.tilWalletPhone);
        edtPhone = view.findViewById(R.id.edtWalletPhone);
        progressBar = view.findViewById(R.id.walletLoadingProgress);
        btnSubmit = view.findViewById(R.id.btnPerformLinkWallet);

        if (chipGroupProvider != null) {
            chipGroupProvider.check(R.id.chipMoMo);
            chipGroupProvider.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipZaloPay) {
                    selectedProvider = "ZaloPay";
                } else {
                    selectedProvider = "MoMo";
                }
                validateForm();
            });
        }

        if (edtPhone != null) {
            edtPhone.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateForm();
                }
                @Override public void afterTextChanged(Editable s) { }
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> performLink());
        }
    }

    private void validateForm() {
        String phone = edtPhone != null ? edtPhone.getText().toString().trim() : "";
        boolean validPhone = phone.matches("^0\\d{9}$");
        if (tilPhone != null) {
            if (!phone.isEmpty() && !validPhone) {
                tilPhone.setError(getString(R.string.wallet_phone_error));
            } else {
                tilPhone.setError(null);
            }
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(validPhone);
            btnSubmit.setAlpha(validPhone ? 1.0f : 0.5f);
        }
    }

    private void performLink() {
        String phone = edtPhone != null ? edtPhone.getText().toString().trim() : "";
        if (!phone.matches("^0\\d{9}$")) return;

        if (btnSubmit != null) btnSubmit.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            PaymentRepository.getInstance().addWallet(selectedProvider, phone);
            if (getActivity() != null && getActivity().findViewById(android.R.id.content) != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        getString(R.string.wallet_linked_toast), Snackbar.LENGTH_SHORT).show();
            }
            if (callback != null) {
                callback.onWalletLinked();
            }
            dismissAllowingStateLoss();
        }, 1000L);
    }
}
