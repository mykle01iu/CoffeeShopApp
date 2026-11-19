package com.coffeeshop.client.catalog.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.data.ProductRepository;
import com.coffeeshop.client.catalog.data.ProductReviewRepository;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.catalog.model.ProductReview;
import com.coffeeshop.client.data.FavoriteRepository;
import com.coffeeshop.client.data.CartRepository;
import com.coffeeshop.client.ui.home.ProductOptionsSheet;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends Activity {
    public static final String EXTRA_PRODUCT_ID = "productId";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_product_detail);

        findViewById(R.id.detailBack).setOnClickListener(v -> finish());

        int id = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        Product product = null;
        for (Product p : ProductRepository.getProducts()) {
            if (p.id == id) {
                product = p;
                break;
            }
        }

        View scroll = findViewById(R.id.detailScroll);
        View error = findViewById(R.id.detailError);

        if (product == null) {
            scroll.setVisibility(View.GONE);
            findViewById(R.id.detailAddToCart).setVisibility(View.GONE);
            error.setVisibility(View.VISIBLE);
            error.findViewById(R.id.notFoundBack).setOnClickListener(v -> finish());
            return;
        }
        Product selectedProduct=product;
        findViewById(R.id.detailAddToCart).setOnClickListener(v->ProductOptionsSheet.show(this,selectedProduct,null,(options,quantity)->{
            CartRepository.add(this,selectedProduct,options,quantity);
            String size=options.size==null?getString(R.string.options_no_size):options.size.label;
            Snackbar.make(findViewById(android.R.id.content),getString(R.string.options_added_snackbar,selectedProduct.name,size,CartRepository.totalQuantity(this)),Snackbar.LENGTH_SHORT).show();
        }));

        ImageButton detailFavorite = findViewById(R.id.detailFavorite);
        if (detailFavorite != null) {
            final Product finalProduct = product;
            boolean isFav = FavoriteRepository.isFavorite(this, product.id);
            updateFavoriteBtn(detailFavorite, isFav);

            detailFavorite.setOnClickListener(v -> {
                boolean newFav = FavoriteRepository.toggleFavorite(this, finalProduct.id);
                updateFavoriteBtn(detailFavorite, newFav);
                Toast.makeText(this, newFav ? "Đã lưu " + finalProduct.name + " vào món yêu thích" : "Đã bỏ " + finalProduct.name + " khỏi món yêu thích", Toast.LENGTH_SHORT).show();
            });
        }

        ((ImageView) findViewById(R.id.detailImage)).setImageResource(product.imageRes);
        ((TextView) findViewById(R.id.detailName)).setText(product.name);
        ((TextView) findViewById(R.id.detailCategory)).setText(product.category);
        ((TextView) findViewById(R.id.detailPrice)).setText(getString(R.string.product_price_format, NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(product.price)));

        TextView rating = findViewById(R.id.detailRating);
        rating.setText(getString(R.string.product_rating_format, stars(product.rating), product.rating, product.reviewCount));
        ((TextView) findViewById(R.id.detailDescription)).setText(product.description);
        ((TextView) findViewById(R.id.reviewCount)).setText(getString(R.string.product_review_count_format, product.reviewCount));

        LinearLayout reviewList = findViewById(R.id.reviewListContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ProductReview review : ProductReviewRepository.getReviews(product)) {
            View row = inflater.inflate(R.layout.item_product_review, reviewList, false);
            ((TextView) row.findViewById(R.id.reviewAuthor)).setText(review.author);
            ((TextView) row.findViewById(R.id.reviewDate)).setText(review.date);
            ((TextView) row.findViewById(R.id.reviewStars)).setText(getString(R.string.product_review_stars_format, stars(review.rating), review.rating));
            ((TextView) row.findViewById(R.id.reviewComment)).setText(review.comment);
            reviewList.addView(row);
        }

        rating.setOnClickListener(v -> ((ScrollView) findViewById(R.id.detailScroll)).smoothScrollTo(0, findViewById(R.id.reviewListContainer).getTop()));
    }

    private void updateFavoriteBtn(ImageButton btn, boolean isFav) {
        if (isFav) {
            btn.setImageResource(R.drawable.ic_favorite_filled);
            btn.setColorFilter(Color.parseColor("#E53935"));
        } else {
            btn.setImageResource(R.drawable.ic_favorite_border);
            btn.setColorFilter(Color.parseColor("#7D6C61"));
        }
    }

    private static String stars(float score) {
        int full = Math.max(0, Math.min(5, Math.round(score)));
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 5; i++) value.append(i < full ? '★' : '☆');
        return value.toString();
    }
}
