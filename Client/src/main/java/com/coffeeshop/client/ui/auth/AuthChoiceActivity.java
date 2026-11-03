package com.coffeeshop.client.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.coffeeshop.client.ui.home.HomeActivity;

public class AuthChoiceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView title = findViewById(R.id.authChoiceTitle);
        title.setTypeface(FontUtils.lora(this, 400));
        TextView brandName = findViewById(R.id.authChoiceBrandName);
        brandName.setTypeface(FontUtils.lora(this, 600));

        Button openLoginButton = findViewById(R.id.openLoginButton);
        openLoginButton.setOnClickListener(view ->
                startActivity(new Intent(this, LoginActivity.class)));

        Button openRegisterButton = findViewById(R.id.openRegisterButton);
        openRegisterButton.setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.exploreMenuLink).setOnClickListener(view -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}
