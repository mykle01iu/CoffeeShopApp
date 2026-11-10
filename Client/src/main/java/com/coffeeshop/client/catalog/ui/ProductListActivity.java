package com.coffeeshop.client.catalog.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.coffeeshop.client.R;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.data.OptionsConfig;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.ui.home.ProductOptionsSheet;
import com.coffeeshop.client.catalog.viewmodel.ProductListViewModel;

public class ProductListActivity extends Activity {
    private final ProductListViewModel viewModel=new ProductListViewModel();
    private EditText search; private ImageButton clear; private LinearLayout chips; private RecyclerView list; private View empty;
    private final int[] categoryLabels={R.string.category_all_catalog,R.string.category_coffee_catalog,R.string.category_tea_catalog,R.string.category_smoothie_catalog,R.string.category_cake_catalog,R.string.category_other_catalog};
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_product_list);
        search=findViewById(R.id.productSearch); clear=findViewById(R.id.clearSearch); chips=findViewById(R.id.categoryChips);
        list=findViewById(R.id.productList); empty=findViewById(R.id.productEmpty);
        ProductAdapter adapter=new ProductAdapter(new ProductAdapter.OnClick(){
            @Override public void onClick(Product p){startActivity(new Intent(ProductListActivity.this,ProductDetailActivity.class).putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID,p.id));}
            @Override public boolean supportsAdd(){return true;}
            @Override public void onAdd(Product p,View anchor){ProductOptionsSheet.show(ProductListActivity.this,p,null,(options,quantity)->{CartRepository.add(ProductListActivity.this,p,options,quantity);String size=options.size==null?getString(R.string.options_no_size):options.size.label;Snackbar.make(findViewById(android.R.id.content),getString(R.string.options_added_snackbar,p.name,size,CartRepository.totalQuantity(ProductListActivity.this)),Snackbar.LENGTH_SHORT).show();});}
        });
        list.setLayoutManager(new LinearLayoutManager(this)); list.setAdapter(adapter); buildChips();
        viewModel.observe(state1 -> {
            adapter.submit(state1.products); list.setVisibility(state1.products.isEmpty()?View.GONE:View.VISIBLE); empty.setVisibility(state1.products.isEmpty()?View.VISIBLE:View.GONE);
            clear.setVisibility(state1.query.isEmpty()?View.GONE:View.VISIBLE);
            for(int i=0;i<chips.getChildCount();i++) { TextView chip=(TextView)chips.getChildAt(i); boolean selected=chip.getText().toString().equals(state1.selectedCategory); chip.setBackgroundResource(selected?R.drawable.bg_category_chip_selected:R.drawable.bg_category_chip); chip.setTextColor(getColor(selected?R.color.coffee_surface:R.color.coffee_muted)); }
        });
        search.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int start,int count,int after){} public void onTextChanged(CharSequence s,int start,int before,int count){viewModel.setQuery(s.toString());} public void afterTextChanged(Editable e){} });
        clear.setOnClickListener(v -> search.setText(""));
        empty.findViewById(R.id.resetFilters).setOnClickListener(v -> { search.setText(""); viewModel.clearFilters(); });
    }
    private void buildChips() {
        for(int label:categoryLabels) {
            String category=getString(label);
            TextView chip=new TextView(this); chip.setText(category); chip.setTextSize(13); chip.setTypeface(null, Typeface.BOLD); chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(16),0,dp(16),0); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(38)); lp.setMarginEnd(dp(8)); chips.addView(chip,lp);
            chip.setOnClickListener(v -> viewModel.setCategory(category));
        }
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
