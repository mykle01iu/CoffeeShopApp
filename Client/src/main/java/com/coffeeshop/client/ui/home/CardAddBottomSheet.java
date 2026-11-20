package com.coffeeshop.client.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.payment.PaymentRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class CardAddBottomSheet extends BottomSheetDialogFragment {
    public interface OnCardAddedCallback {
        void onCardAdded();
    }

    private OnCardAddedCallback callback;
    private TextInputLayout tilCardNumber;
    private TextInputLayout tilCardHolder;
    private TextInputLayout tilCardExpiry;
    private TextInputLayout tilCardCvv;

    private EditText edtCardNumber;
    private EditText edtCardHolder;
    private EditText edtCardExpiry;
    private EditText edtCardCvv;
    private Button btnSubmitCardSave;

    private boolean isFormattingCard = false;
    private boolean isFormattingExpiry = false;

    public void setCallback(OnCardAddedCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_card_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilCardNumber = view.findViewById(R.id.tilCardNumber);
        tilCardHolder = view.findViewById(R.id.tilCardHolder);
        tilCardExpiry = view.findViewById(R.id.tilCardExpiry);
        tilCardCvv = view.findViewById(R.id.tilCardCvv);

        edtCardNumber = view.findViewById(R.id.edtCardNumber);
        edtCardHolder = view.findViewById(R.id.edtCardHolder);
        edtCardExpiry = view.findViewById(R.id.edtCardExpiry);
        edtCardCvv = view.findViewById(R.id.edtCardCvv);
        btnSubmitCardSave = view.findViewById(R.id.btnSubmitCardSave);

        setupFormattingAndValidation();
        btnSubmitCardSave.setOnClickListener(v -> submitCard());
    }

    private void setupFormattingAndValidation() {
        // Formatting Card Number (Group by 4 spaces)
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (isFormattingCard) return;
                isFormattingCard = true;

                String digitsOnly = s.toString().replaceAll("\\s+", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digitsOnly.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isFormattingCard = false;
                validateForm();
            }
        });

        // Formatting Expiry MM/YY (Auto '/' insert)
        edtCardExpiry.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (isFormattingExpiry) return;
                isFormattingExpiry = true;

                String clean = s.toString().replaceAll("[^\\d]", "");
                StringBuilder formatted = new StringBuilder();
                if (clean.length() >= 2) {
                    formatted.append(clean.substring(0, 2)).append("/");
                    if (clean.length() > 2) {
                        formatted.append(clean.substring(2, Math.min(clean.length(), 4)));
                    }
                } else {
                    formatted.append(clean);
                }

                s.replace(0, s.length(), formatted.toString());
                isFormattingExpiry = false;
                validateForm();
            }
        });

        // Name & CVV text listeners
        TextWatcher genericWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) { }
        };
        edtCardHolder.addTextChangedListener(genericWatcher);
        edtCardCvv.addTextChangedListener(genericWatcher);
    }

    private boolean validateForm() {
        String numStr = edtCardNumber.getText() != null ? edtCardNumber.getText().toString().replaceAll("\\s+", "") : "";
        String holderStr = edtCardHolder.getText() != null ? edtCardHolder.getText().toString().trim() : "";
        String expiryStr = edtCardExpiry.getText() != null ? edtCardExpiry.getText().toString().trim() : "";
        String cvvStr = edtCardCvv.getText() != null ? edtCardCvv.getText().toString().trim() : "";

        boolean isCardValid = isLuhnValid(numStr);
        boolean isHolderValid = !holderStr.isEmpty();
        boolean isExpiryValid = isExpiryValid(expiryStr);
        boolean isCvvValid = cvvStr.matches("^\\d{3,4}$");

        // Error highlights
        if (numStr.length() >= 13 && !isCardValid) {
            tilCardNumber.setError(getString(R.string.card_number_error));
        } else {
            tilCardNumber.setError(null);
        }

        if (expiryStr.length() == 5 && !isExpiryValid) {
            tilCardExpiry.setError(getString(R.string.card_expiry_error));
        } else {
            tilCardExpiry.setError(null);
        }

        if (cvvStr.length() > 0 && !isCvvValid) {
            tilCardCvv.setError(getString(R.string.card_cvv_error));
        } else {
            tilCardCvv.setError(null);
        }

        boolean allValid = isCardValid && isHolderValid && isExpiryValid && isCvvValid;
        btnSubmitCardSave.setEnabled(allValid);
        btnSubmitCardSave.setAlpha(allValid ? 1.0f : 0.5f);
        return allValid;
    }

    private void submitCard() {
        if (!validateForm()) return;

        String rawNum = edtCardNumber.getText().toString().replaceAll("\\s+", "");
        String expiry = edtCardExpiry.getText().toString().trim();
        String brand = detectBrand(rawNum);
        String last4 = rawNum.length() >= 4 ? rawNum.substring(rawNum.length() - 4) : "0000";

        // Save brand, last 4 digits, expiry ONLY
        PaymentRepository.getInstance().addCard(brand, last4, expiry);

        // SECURELY CLEAR SENSITIVE STATE (Full Card Number & CVV)
        edtCardNumber.setText("");
        edtCardCvv.setText("");
        edtCardHolder.setText("");

        if (getActivity() != null) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    getString(R.string.card_saved_toast), Snackbar.LENGTH_SHORT).show();
        }

        if (callback != null) {
            callback.onCardAdded();
        }
        dismiss();
    }

    public static boolean isLuhnValid(String numberStr) {
        if (numberStr == null) return false;
        String clean = numberStr.replaceAll("\\s+", "");
        if (clean.length() < 13 || clean.length() > 19) return false;
        int sum = 0;
        boolean alternate = false;
        for (int i = clean.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(clean.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private static String detectBrand(String number) {
        if (number == null || number.isEmpty()) return "Visa";
        if (number.startsWith("4")) return "Visa";
        if (number.matches("^(5[1-5]|2[2-7]).*")) return "Mastercard";
        if (number.matches("^3[47].*")) return "Amex";
        if (number.matches("^35.*")) return "JCB";
        return "Visa";
    }

    private static boolean isExpiryValid(String expiry) {
        if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) return false;
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);

            Calendar current = Calendar.getInstance();
            int curYear = current.get(Calendar.YEAR);
            int curMonth = current.get(Calendar.MONTH) + 1;

            if (year > curYear) return true;
            return (year == curYear && month >= curMonth);
        } catch (Exception e) {
            return false;
        }
    }
}
