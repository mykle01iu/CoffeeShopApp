package com.coffeeshop.client.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.PointsRepository;
import com.coffeeshop.client.model.PointTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class PointsHistoryBottomSheet extends BottomSheetDialogFragment {

    private int currentFilter = 0; // 0: All, 1: Received, 2: Used
    private TextView chipAll, chipReceived, chipUsed;
    private RecyclerView recyclerView;
    private View emptyView;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_points_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int totalPoints = PointsRepository.getTotalPoints();
        TextView pointsTotalText = view.findViewById(R.id.sheetPointsTotalText);
        ProgressBar pointsProgressBar = view.findViewById(R.id.sheetPointsProgressBar);
        TextView tierHintText = view.findViewById(R.id.sheetTierHintText);

        pointsTotalText.setText(totalPoints + " điểm");
        pointsProgressBar.setMax(200);
        pointsProgressBar.setProgress(totalPoints);
        int remaining = Math.max(0, 200 - totalPoints);
        tierHintText.setText("Còn " + remaining + " điểm nữa lên Hạng Vàng");

        chipAll = view.findViewById(R.id.chipAll);
        chipReceived = view.findViewById(R.id.chipReceived);
        chipUsed = view.findViewById(R.id.chipUsed);

        recyclerView = view.findViewById(R.id.pointsRecyclerView);
        emptyView = view.findViewById(R.id.pointsEmptyView);

        adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        chipAll.setOnClickListener(v -> selectFilter(0));
        chipReceived.setOnClickListener(v -> selectFilter(1));
        chipUsed.setOnClickListener(v -> selectFilter(2));

        Button btnRedeem = view.findViewById(R.id.btnRedeemGift);
        Button btnClose = view.findViewById(R.id.btnCloseSheet);

        btnRedeem.setOnClickListener(v -> Toast.makeText(getContext(), "Tính năng đổi quà sắp ra mắt!", Toast.LENGTH_SHORT).show());
        btnClose.setOnClickListener(v -> dismiss());

        selectFilter(0);
    }

    private void selectFilter(int filterType) {
        currentFilter = filterType;
        updateChipStyles();
        List<PointTransaction> list = PointsRepository.getFilteredTransactions(filterType);
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
        updateChip(chipAll, currentFilter == 0);
        updateChip(chipReceived, currentFilter == 1);
        updateChip(chipUsed, currentFilter == 2);
    }

    private void updateChip(TextView chip, boolean isSelected) {
        if (chip == null || getContext() == null) return;
        chip.setBackgroundResource(isSelected ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
        chip.setTextColor(getContext().getColor(isSelected ? R.color.coffee_surface : R.color.coffee_muted));
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {
        private final List<PointTransaction> items = new ArrayList<>();

        void submit(List<PointTransaction> transactions) {
            items.clear();
            if (transactions != null) items.addAll(transactions);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point_transaction, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            PointTransaction item = items.get(position);
            h.title.setText(item.title);
            h.date.setText(item.date);

            if (item.points > 0) {
                h.points.setText("+" + item.points);
                h.points.setTextColor(Color.parseColor("#43A047"));
                h.icon.setImageResource(R.drawable.ic_arrow_up_right);
                h.icon.setColorFilter(Color.parseColor("#43A047"));
            } else {
                h.points.setText(String.valueOf(item.points));
                h.points.setTextColor(Color.parseColor("#E53935"));
                h.icon.setImageResource(R.drawable.ic_arrow_back);
                h.icon.setColorFilter(Color.parseColor("#E53935"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title, date, points;
            final ImageView icon;

            Holder(View v) {
                super(v);
                title = v.findViewById(R.id.transactionTitle);
                date = v.findViewById(R.id.transactionDate);
                points = v.findViewById(R.id.transactionPoints);
                icon = v.findViewById(R.id.transactionIcon);
            }
        }
    }
}
