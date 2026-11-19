package com.coffeeshop.client.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.VoucherRepository;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherStatus;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class VoucherPickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }

    private final int subtotal;
    private final OnVoucherSelectedListener listener;

    private EditText voucherInput;
    private TextView voucherError;
    private RecyclerView recyclerView;
    private View emptyView;

    public VoucherPickerBottomSheet(int subtotal, OnVoucherSelectedListener listener) {
        this.subtotal = subtotal;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_voucher_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        voucherInput = view.findViewById(R.id.pickerVoucherInput);
        voucherError = view.findViewById(R.id.pickerVoucherError);
        recyclerView = view.findViewById(R.id.pickerRecyclerView);
        emptyView = view.findViewById(R.id.pickerEmptyView);

        Button btnApply = view.findViewById(R.id.btnPickerApply);
        btnApply.setOnClickListener(v -> applyTypedCode());

        List<Voucher> activeVouchers = VoucherRepository.getVouchersByStatus(VoucherStatus.ACTIVE);
        PickerAdapter adapter = new PickerAdapter(activeVouchers, subtotal, getContext(), voucher -> {
            if (listener != null) listener.onVoucherSelected(voucher);
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (activeVouchers.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void applyTypedCode() {
        String code = voucherInput.getText().toString().trim();
        if (code.isEmpty()) {
            voucherError.setText("Vui lòng nhập mã voucher");
            voucherError.setVisibility(View.VISIBLE);
            return;
        }

        String validationError = VoucherRepository.validate(getContext(), code, subtotal);
        if (validationError != null) {
            voucherError.setText(validationError);
            voucherError.setVisibility(View.VISIBLE);
        } else {
            voucherError.setVisibility(View.GONE);
            Voucher voucher = VoucherRepository.getVoucherByCode(code);
            if (listener != null) listener.onVoucherSelected(voucher);
            dismiss();
        }
    }

    private static class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.Holder> {
        interface OnSelect { void onSelect(Voucher v); }
        private final List<Voucher> items = new ArrayList<>();
        private final int subtotal;
        private final Context context;
        private final OnSelect listener;

        PickerAdapter(List<Voucher> vouchers, int subtotal, Context context, OnSelect listener) {
            if (vouchers != null) this.items.addAll(vouchers);
            this.subtotal = subtotal;
            this.context = context;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Voucher v = items.get(position);
            h.title.setText(v.title);
            h.code.setText(v.code);
            h.icon.setImageResource(v.iconRes);

            String errorMsg = VoucherRepository.validate(context, v.code, subtotal);
            boolean isEligible = (errorMsg == null);

            h.condition.setText(isEligible ? v.condition : errorMsg);
            h.condition.setTextColor(isEligible ? Color.parseColor("#7D6C61") : Color.parseColor("#E53935"));
            h.note.setText(v.note);

            h.cardRoot.setAlpha(isEligible ? 1.0f : 0.45f);

            h.btnCopy.setVisibility(View.GONE);
            h.btnUseNow.setText("Dùng");
            h.btnUseNow.setVisibility(View.VISIBLE);
            h.btnUseNow.setEnabled(isEligible);

            if (isEligible) {
                h.btnUseNow.setOnClickListener(view -> {
                    if (listener != null) listener.onSelect(v);
                });
            } else {
                h.btnUseNow.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final View cardRoot;
            final TextView title, condition, note, code;
            final ImageView icon;
            final Button btnCopy, btnUseNow;

            Holder(View v) {
                super(v);
                cardRoot = v.findViewById(R.id.voucherCardRoot);
                title = v.findViewById(R.id.voucherTitle);
                condition = v.findViewById(R.id.voucherCondition);
                note = v.findViewById(R.id.voucherNote);
                code = v.findViewById(R.id.voucherCode);
                icon = v.findViewById(R.id.voucherIcon);
                btnCopy = v.findViewById(R.id.btnCopyVoucherCode);
                btnUseNow = v.findViewById(R.id.btnUseVoucherNow);
            }
        }
    }
}
