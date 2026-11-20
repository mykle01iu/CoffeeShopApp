package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.payment.PaymentMethod;
import com.coffeeshop.client.data.payment.PaymentRepository;
import com.coffeeshop.client.data.payment.PaymentType;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PaymentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView title = findViewById(R.id.paymentMethodsTitle);
        if (title != null) {
            title.setTypeface(FontUtils.lora(this, 600));
        }

        ImageButton backBtn = findViewById(R.id.paymentMethodsBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.paymentMethodsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PaymentAdapter(new PaymentAdapter.OnPaymentActionListener() {
            @Override
            public void onSetDefault(PaymentMethod method) {
                PaymentRepository.getInstance().setDefault(method.getId());
                showSnackbar(getString(R.string.payment_set_default_toast, method.getDisplayName()));
            }

            @Override
            public void onDelete(PaymentMethod method) {
                confirmDelete(method);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnLinkWallet).setOnClickListener(v -> showWalletLinkSheet());
        findViewById(R.id.btnAddCard).setOnClickListener(v -> showCardAddSheet());

        PaymentRepository.getInstance().getMethodsLiveData().observe(this, methods -> {
            if (methods != null) {
                adapter.submitList(methods);
            }
        });
    }

    private void showWalletLinkSheet() {
        WalletLinkBottomSheet sheet = new WalletLinkBottomSheet();
        sheet.show(getSupportFragmentManager(), "WalletLinkBottomSheet");
    }

    private void showCardAddSheet() {
        CardAddBottomSheet sheet = new CardAddBottomSheet();
        sheet.show(getSupportFragmentManager(), "CardAddBottomSheet");
    }

    private void confirmDelete(PaymentMethod method) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.payment_delete_confirm_title)
                .setMessage(getString(R.string.payment_delete_confirm_message, method.getDisplayName()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.payment_delete, (dialog, which) -> {
                    PaymentRepository.getInstance().remove(method.getId());
                    showSnackbar(getString(R.string.payment_deleted_toast, method.getDisplayName()));
                })
                .show();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    // Adapter Class
    private static class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.Holder> {
        interface OnPaymentActionListener {
            void onSetDefault(PaymentMethod method);
            void onDelete(PaymentMethod method);
        }

        private final List<PaymentMethod> items = new ArrayList<>();
        private final OnPaymentActionListener listener;

        PaymentAdapter(OnPaymentActionListener listener) {
            this.listener = listener;
        }

        void submitList(List<PaymentMethod> methods) {
            items.clear();
            if (methods != null) items.addAll(methods);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PaymentMethod item = items.get(position);
            holder.name.setText(item.getDisplayName());
            holder.detail.setText(item.getDetail());
            holder.defaultBadge.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);

            // Icon according to type
            if (item.getType() == PaymentType.COD) {
                holder.icon.setImageResource(R.drawable.ic_orders);
            } else if (item.getType() == PaymentType.WALLET) {
                holder.icon.setImageResource(R.drawable.ic_phone);
            } else {
                holder.icon.setImageResource(R.drawable.ic_location);
            }

            holder.overflowBtn.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                if (!item.isDefault()) {
                    popup.getMenu().add(0, 1, 0, R.string.payment_set_default);
                }
                if (item.canDelete()) {
                    popup.getMenu().add(0, 2, 1, R.string.payment_delete);
                }
                popup.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == 1) {
                        listener.onSetDefault(item);
                        return true;
                    } else if (menuItem.getItemId() == 2) {
                        listener.onDelete(item);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView name, detail, defaultBadge;
            final ImageButton overflowBtn;

            Holder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.paymentIcon);
                name = itemView.findViewById(R.id.paymentNameText);
                detail = itemView.findViewById(R.id.paymentDetailText);
                defaultBadge = itemView.findViewById(R.id.paymentDefaultBadge);
                overflowBtn = itemView.findViewById(R.id.btnPaymentOverflow);
            }
        }
    }
}
