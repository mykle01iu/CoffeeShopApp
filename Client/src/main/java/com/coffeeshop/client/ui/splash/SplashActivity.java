package com.coffeeshop.client.ui.splash;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.auth.AuthChoiceActivity;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class SplashActivity extends Activity {
    private static final long MIN_SPLASH_MILLIS = 2100L;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long splashStartTime;
    private boolean initializationComplete;
    private final Runnable openWelcome = () -> {
        if (isFinishing() || isDestroyed()) return;
        startActivity(new Intent(this, AuthChoiceActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashStartTime = SystemClock.uptimeMillis();
        setContentView(R.layout.activity_splash);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView brandName = findViewById(R.id.splashBrandName);
        Typeface serif = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                ? Typeface.create(Typeface.create("serif", Typeface.NORMAL), 600, false)
                : Typeface.create("serif", Typeface.BOLD);
        brandName.setTypeface(serif);

        View splashContent = findViewById(R.id.splashContent);
        ViewTreeObserver observer = splashContent.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewTreeObserver currentObserver = splashContent.getViewTreeObserver();
                if (currentObserver.isAlive()) {
                    currentObserver.removeOnPreDrawListener(this);
                }
                initializationComplete = true;
                scheduleWelcomeWhenReady();
                return true;
            }
        });
    }

    private void scheduleWelcomeWhenReady() {
        if (!initializationComplete || isFinishing() || isDestroyed()) return;
        long minimumDuration = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !ValueAnimator.areAnimatorsEnabled() ? 0L : MIN_SPLASH_MILLIS;
        long remaining = minimumDuration - (SystemClock.uptimeMillis() - splashStartTime);
        if (remaining <= 0) {
            openWelcome.run();
        } else {
            mainHandler.postDelayed(openWelcome, remaining);
        }
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(openWelcome);
        super.onDestroy();
    }
}
