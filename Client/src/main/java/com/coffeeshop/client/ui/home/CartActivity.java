package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.catalog.ui.ProductListActivity;
import com.coffeeshop.client.data.CartRepository;
import com.google.android.material.snackbar.Snackbar;
import com.coffeeshop.client.model.CartItem;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends Activity {

    private RecyclerView recyclerView;
    private View emptyView, bottomBar;
    private TextView subtotalText;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.cartTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.cartBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.cartRecyclerView);
        emptyView = findViewById(R.id.cartEmptyView);
        bottomBar = findViewById(R.id.cartBottomBar);
        subtotalText = findViewById(R.id.cartSubtotalText);

        findViewById(R.id.btnBrowseMenu).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductListActivity.class));
            finish();
        });

        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        adapter = new CartAdapter(new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncrease(CartItem item) {
                CartRepository.increase(CartActivity.this, item);
                loadCart();
            }

            @Override
            public void onDecrease(CartItem item) {
                CartRepository.decrease(CartActivity.this, item);
                loadCart();
            }

            @Override
            public void onRemove(CartItem item) {
                CartRepository.remove(CartActivity.this, item);
                loadCart();
            }
            @Override public void onEdit(CartItem item, View anchor) {
                ProductOptionsSheet.show(CartActivity.this,item.product,item,(options,quantity)->{
                    CartRepository.update(CartActivity.this,item,options,quantity); loadCart();
                    Snackbar.make(findViewById(android.R.id.content),getString(R.string.options_updated_snackbar,item.product.name,CartRepository.totalQuantity(CartActivity.this)),Snackbar.LENGTH_SHORT).show();
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        List<CartItem> items = CartRepository.getItems(this);
        if (items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            adapter.submit(items);
            int subtotal = CartRepository.subtotal(this);
            subtotalText.setText(NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(subtotal) + "đ");
        }
    }

    public static class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {
        public interface OnCartActionListener {
            void onIncrease(CartItem item);
            void onDecrease(CartItem item);
            void onRemove(CartItem item);
            void onEdit(CartItem item, View anchor);
        }

        private final List<CartItem> items = new ArrayList<>();
        private final OnCartActionListener listener;

        public CartAdapter(OnCartActionListener listener) {
            this.listener = listener;
        }

        public void submit(List<CartItem> cartItems) {
            items.clear();
            if (cartItems != null) items.addAll(cartItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_line, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            CartItem item = items.get(position);
            Product p = item.product;

            h.name.setText(p.name);
            h.price.setText(h.itemView.getContext().getString(R.string.money_vnd,NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(item.getLineTotal())));
            h.qty.setText(String.valueOf(item.quantity));
            h.image.setImageResource(p.imageRes);
            String summary=item.getOptionSummary(h.itemView.getContext());h.options.setText(summary);h.options.setVisibility(summary.isEmpty()?View.GONE:View.VISIBLE);
            h.note.setText(h.itemView.getContext().getString(R.string.cart_note_format,item.options.note));h.note.setVisibility(item.options.note.isEmpty()?View.GONE:View.VISIBLE);

            h.btnPlus.setOnClickListener(v -> { if (listener != null) listener.onIncrease(item); });
            h.btnMinus.setOnClickListener(v -> { if (listener != null) listener.onDecrease(item); });
            h.btnRemove.setOnClickListener(v -> { if (listener != null) listener.onRemove(item); });
            h.itemView.setOnClickListener(v->{if(listener!=null)listener.onEdit(item,v);});
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView name, price, qty, btnMinus, btnPlus, options, note;
            final ImageView image;
            final ImageButton btnRemove;

            Holder(View v) {
                super(v);
                name = v.findViewById(R.id.cartItemName);
                price = v.findViewById(R.id.cartItemPrice);
                options = v.findViewById(R.id.cartItemOptions); note=v.findViewById(R.id.cartItemNote);
                qty = v.findViewById(R.id.cartItemQty);
                btnMinus = v.findViewById(R.id.btnCartMinus);
                btnPlus = v.findViewById(R.id.btnCartPlus);
                image = v.findViewById(R.id.cartItemImage);
                btnRemove = v.findViewById(R.id.btnRemoveCartItem);
            }
        }
    }
}
