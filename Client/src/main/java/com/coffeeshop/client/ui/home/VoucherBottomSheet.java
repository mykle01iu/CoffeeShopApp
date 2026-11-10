package com.coffeeshop.client.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.ui.ProductListActivity;
import com.coffeeshop.client.data.VoucherRepository;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherStatus;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class VoucherBottomSheet extends BottomSheetDialogFragment {

    private VoucherStatus currentStatus = VoucherStatus.ACTIVE;
    private TextView subtitleText, chipActive, chipUsed, chipExpired;
    private RecyclerView recyclerView;
    private View emptyView;
    private VoucherAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_vouchers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subtitleText = view.findViewById(R.id.voucherHeaderSubtitle);
        int activeCount = VoucherRepository.getActiveCount();
        subtitleText.setText(activeCount + " mã có hiệu lực");

        chipActive = view.findViewById(R.id.chipVoucherActive);
        chipUsed = view.findViewById(R.id.chipVoucherUsed);
        chipExpired = view.findViewById(R.id.chipVoucherExpired);

        chipActive.setText("Có thể dùng (" + activeCount + ")");
        chipUsed.setText("Đã dùng (" + VoucherRepository.getVoucherCount(VoucherStatus.USED) + ")");
        chipExpired.setText("Hết hạn (" + VoucherRepository.getVoucherCount(VoucherStatus.EXPIRED) + ")");

        recyclerView = view.findViewById(R.id.vouchersRecyclerView);
        emptyView = view.findViewById(R.id.vouchersEmptyView);

        adapter = new VoucherAdapter(new VoucherAdapter.OnVoucherActionListener() {
            @Override
            public void onCopy(String code) {
                copyVoucherCode(code);
            }

            @Override
            public void onUseNow(Voucher voucher) {
                if (getContext() == null) return;
                VoucherRepository.setSelectedVoucherCode(getContext(), voucher.code);
                Toast.makeText(getContext(), "Mã " + voucher.code + " sẽ được áp dụng ở bước thanh toán", Toast.LENGTH_LONG).show();
                dismiss();
                startActivity(new Intent(getContext(), ProductListActivity.class));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        chipActive.setOnClickListener(v -> selectStatus(VoucherStatus.ACTIVE));
        chipUsed.setOnClickListener(v -> selectStatus(VoucherStatus.USED));
        chipExpired.setOnClickListener(v -> selectStatus(VoucherStatus.EXPIRED));

        Button btnClose = view.findViewById(R.id.btnCloseVoucherSheet);
        btnClose.setOnClickListener(v -> dismiss());

        selectStatus(VoucherStatus.ACTIVE);
    }

    private void selectStatus(VoucherStatus status) {
        currentStatus = status;
        updateChipStyles();
        List<Voucher> list = VoucherRepository.getVouchersByStatus(status);
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.submit(list);
        }
    }

    private void updateChipStyles() {
        if (getContext() == null) return;
        updateChip(chipActive, currentStatus == VoucherStatus.ACTIVE);
        updateChip(chipUsed, currentStatus == VoucherStatus.USED);
        updateChip(chipExpired, currentStatus == VoucherStatus.EXPIRED);
    }

    private void updateChip(TextView chip, boolean isSelected) {
        if (chip == null || getContext() == null) return;
        chip.setBackgroundResource(isSelected ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
        chip.setTextColor(getContext().getColor(isSelected ? R.color.coffee_surface : R.color.coffee_muted));
    }

    private void copyVoucherCode(String code) {
        if (getContext() == null) return;
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Voucher Mộc Miên", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Đã sao chép mã " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private static class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.Holder> {
        interface OnVoucherActionListener {
            void onCopy(String code);
            void onUseNow(Voucher voucher);
        }
        private final List<Voucher> items = new ArrayList<>();
        private final OnVoucherActionListener listener;

        VoucherAdapter(OnVoucherActionListener listener) { this.listener = listener; }

        void submit(List<Voucher> vouchers) {
            items.clear();
            if (vouchers != null) items.addAll(vouchers);
            notifyDataSetChanged();
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
            h.condition.setText(v.condition);
            h.note.setText(v.note);
            h.code.setText(v.code);
            h.icon.setImageResource(v.iconRes);

            boolean isActive = v.status == VoucherStatus.ACTIVE;
            h.cardRoot.setAlpha(isActive ? 1.0f : 0.5f);

            if (isActive) {
                h.btnUseNow.setVisibility(View.VISIBLE);
                h.btnCopy.setVisibility(View.VISIBLE);

                h.btnUseNow.setOnClickListener(view -> {
                    if (listener != null) listener.onUseNow(v);
                });
                h.btnCopy.setOnClickListener(view -> {
                    if (listener != null) listener.onCopy(v.code);
                });
            } else {
                h.btnUseNow.setVisibility(View.GONE);
                h.btnCopy.setVisibility(View.GONE);
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
