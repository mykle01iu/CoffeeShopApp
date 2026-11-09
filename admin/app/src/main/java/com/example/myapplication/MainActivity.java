package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.AccountStore;
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.data.CategoryRepository;
import com.example.myapplication.data.ProductRepository;
import com.example.myapplication.model.Category;
import com.example.myapplication.ui.DashboardScreen;
import com.example.myapplication.ui.LoginScreen;
import com.example.myapplication.ui.CategoryScreen;
import com.example.myapplication.ui.Navigator;
import com.example.myapplication.ui.RevenueScreen;
import com.example.myapplication.ui.ProductScreen;

public class MainActivity extends AppCompatActivity implements Navigator {

    private SessionManager session;
    private AccountStore accounts;
    private CategoryRepository categories;
    private ProductRepository products;
    private ActivityResultLauncher<String[]> imagePicker;
    private ProductImageCallback pendingImageCallback;
    private OnBackPressedCallback backCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable openLogin;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        imagePicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) { }
                if (pendingImageCallback != null) pendingImageCallback.onImagePicked(uri);
            }
            pendingImageCallback = null;
        });
        session = new SessionManager(this);
        accounts = new AccountStore(this, session);
        categories = new CategoryRepository();
        products = new ProductRepository();

        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                showDashboard();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        // Nếu còn session hợp lệ thì vào thẳng Dashboard, không thì về Login
        if (session.isLoggedIn()) {
            showDashboard();
        } else {
            showSplash();
        }
    }

    private void showSplash() {
        setContentView(R.layout.splash);
        openLogin = this::showLoginAfterSplash;
        handler.postDelayed(openLogin, 1500);
    }

    private void showLoginAfterSplash() {
        showLogin();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        if (openLogin != null) handler.removeCallbacks(openLogin);
        super.onDestroy();
    }

    @Override
    public void showLogin() {
        backCallback.setEnabled(false);
        new LoginScreen(this, this, accounts).show();
    }

    @Override
    public void logout() {
        session.clear();
        showLogin();
    }

    @Override
    public void showDashboard() {
        if (!requireLogin()) return;
        backCallback.setEnabled(false);
        new DashboardScreen(this, this, accounts).show();
    }

    @Override
    public void showRevenue() {
        if (!requireLogin()) return;
        backCallback.setEnabled(true);
        new RevenueScreen(this, this).show();
    }

    @Override
    public void showCategories() {
        if (!requireLogin()) return;
        backCallback.setEnabled(true);
        new CategoryScreen(this, this, categories, products).show();
    }

    @Override
    public void showProducts(Category category) {
        if (!requireLogin()) return;
        backCallback.setEnabled(true);
        new ProductScreen(this, this, categories, products, category).show();
    }

    @Override
    public void pickProductImage(ProductImageCallback callback) {
        pendingImageCallback = callback;
        imagePicker.launch(new String[]{"image/*"});
    }

    // Chặn mọi màn hình nội bộ khi chưa có session
    private boolean requireLogin() {
        if (session.isLoggedIn()) return true;
        Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_SHORT).show();
        showLogin();
        return false;
    }
}
