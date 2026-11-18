package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.AccountStore;
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.data.AuthApi;
import com.example.myapplication.data.BackendApi;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Order;
import com.example.myapplication.ui.DashboardScreen;
import com.example.myapplication.ui.LoginScreen;
import com.example.myapplication.ui.CategoryScreen;
import com.example.myapplication.ui.Navigator;
import com.example.myapplication.ui.RevenueScreen;
import com.example.myapplication.ui.ProductScreen;
import com.example.myapplication.ui.OrderScreen;
import com.example.myapplication.ui.OrderDetailScreen;

public class MainActivity extends AppCompatActivity
        implements Navigator, LoginScreen.LocalNetworkPermissionGate {

    private static final String ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK";

    private SessionManager session;
    private AccountStore accounts;
    private BackendApi backendApi;
    private AuthApi authApi;
    private androidx.activity.result.ActivityResultLauncher<String> localNetworkPermissionLauncher;
    private Runnable pendingPermissionGranted;
    private Runnable pendingPermissionDenied;
    private OnBackPressedCallback backCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable openLogin;
    private boolean showingOrderDetail;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        localNetworkPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    Runnable callback = granted ? pendingPermissionGranted : pendingPermissionDenied;
                    pendingPermissionGranted = null;
                    pendingPermissionDenied = null;
                    if (callback != null) callback.run();
                });
        session = new SessionManager(this);
        accounts = new AccountStore(this, session);
        authApi = new AuthApi();
        backendApi = new BackendApi(session);

        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (showingOrderDetail) showOrders();
                else showDashboard();
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
        new LoginScreen(this, this, accounts, session, authApi, this).show();
    }

    @Override
    public void ensureLocalNetworkPermission(Runnable onGranted, Runnable onDenied) {
        if (Build.VERSION.SDK_INT < 37
                || checkSelfPermission(ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED) {
            onGranted.run();
            return;
        }
        pendingPermissionGranted = onGranted;
        pendingPermissionDenied = onDenied;
        localNetworkPermissionLauncher.launch(ACCESS_LOCAL_NETWORK);
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
        showingOrderDetail = false;
        new DashboardScreen(this, this, accounts).show();
    }

    @Override
    public void showOrders() {
        if (!requireLogin()) return;
        showingOrderDetail = false;
        backCallback.setEnabled(true);
        new OrderScreen(this, this).show();
    }

    @Override
    public void showOrderDetail(Order order) {
        if (!requireLogin()) return;
        showingOrderDetail = true;
        backCallback.setEnabled(true);
        new OrderDetailScreen(this, this, order).show();
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
        new CategoryScreen(this, this, backendApi, session).show();
    }

    @Override
    public void showProducts(Category category) {
        if (!requireLogin()) return;
        backCallback.setEnabled(true);
        new ProductScreen(this, this, backendApi, session, category).show();
    }

    // Chặn mọi màn hình nội bộ khi chưa có session
    private boolean requireLogin() {
        if (session.isLoggedIn()) return true;
        Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_SHORT).show();
        showLogin();
        return false;
    }
}
