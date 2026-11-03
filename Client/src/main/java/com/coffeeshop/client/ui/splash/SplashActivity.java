package com.coffeeshop.client.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.auth.AuthChoiceActivity;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class SplashActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openLogin = () -> {
        startActivity(new Intent(this, AuthChoiceActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView brandName = findViewById(R.id.splashBrandName);
        brandName.setTypeface(FontUtils.lora(this, 500));

        ImageView coffeePhoto = findViewById(R.id.splashCoffeePhoto);
        AnimatorSet photoMotion = new AnimatorSet();
        photoMotion.playTogether(
                ObjectAnimator.ofFloat(coffeePhoto, "scaleX", 1f, 1.04f),
                ObjectAnimator.ofFloat(coffeePhoto, "scaleY", 1f, 1.04f)
        );
        photoMotion.setDuration(2000);
        photoMotion.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        photoMotion.start();

        ImageView logo = findViewById(R.id.splashLogo);
        logo.setAlpha(0f);
        logo.setScaleX(0.82f);
        logo.setScaleY(0.82f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(
                ObjectAnimator.ofFloat(logo, "rotation", -18f, 342f),
                ObjectAnimator.ofFloat(logo, "scaleX", 0.82f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.82f, 1f),
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
        );
        entrance.setDuration(1200);
        entrance.setInterpolator(new DecelerateInterpolator(1.5f));
        entrance.start();

        handler.postDelayed(openLogin, 2000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openLogin);
        super.onDestroy();
    }
}
