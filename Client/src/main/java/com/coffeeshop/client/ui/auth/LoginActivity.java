package com.coffeeshop.client.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.coffeeshop.client.ui.home.HomeActivity;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.loginBrandText)).setTypeface(FontUtils.lora(this, 600));
        ((TextView) findViewById(R.id.loginFormTitle)).setTypeface(FontUtils.lora(this, 400));

        EditText identifierInput = findViewById(R.id.emailInput);
        String savedIdentifier = getIntent().getStringExtra(LocalAuthRepository.EXTRA_IDENTIFIER);
        if (savedIdentifier != null) identifierInput.setText(savedIdentifier);
        EditText passwordInput = findViewById(R.id.passwordInput);
        TextView errorText = findViewById(R.id.loginErrorText);
        ImageButton passwordVisibilityButton = findViewById(R.id.passwordVisibilityButton);
        Button loginButton = findViewById(R.id.loginButton);

        findViewById(R.id.loginBackButton).setOnClickListener(view -> finish());

        passwordVisibilityButton.setOnClickListener(view -> {
            int cursorPosition = passwordInput.getSelectionStart();
            boolean showingPassword = passwordInput.getTransformationMethod()
                    instanceof HideReturnsTransformationMethod;
            if (showingPassword) {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordVisibilityButton.setImageResource(R.drawable.ic_visibility);
                passwordVisibilityButton.setContentDescription(getString(R.string.show_password));
            } else {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibilityButton.setImageResource(R.drawable.ic_visibility_off);
                passwordVisibilityButton.setContentDescription(getString(R.string.hide_password));
            }
            if (cursorPosition >= 0) passwordInput.setSelection(cursorPosition);
        });

        loginButton.setOnClickListener(view -> {
            String identifier = identifierInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (identifier.isEmpty()) {
                errorText.setText(R.string.login_error_identifier_required);
                errorText.setVisibility(View.VISIBLE);
                identifierInput.requestFocus();
                return;
            }
            if (!AuthValidation.isValidIdentifier(identifier)) {
                errorText.setText(R.string.auth_identifier_invalid);
                errorText.setVisibility(View.VISIBLE);
                identifierInput.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                errorText.setText(R.string.login_error_password_required);
                errorText.setVisibility(View.VISIBLE);
                passwordInput.requestFocus();
                return;
            }
            if (!AuthValidation.isStrongEnoughPassword(password)) {
                errorText.setText(R.string.auth_password_rule);
                errorText.setVisibility(View.VISIBLE);
                passwordInput.requestFocus();
                return;
            }

            errorText.setVisibility(View.GONE);
            loginButton.setEnabled(false);
            loginButton.setAlpha(0.65f);
            loginButton.setText(R.string.login_processing);
            new Thread(() -> {
                boolean authenticated = LocalAuthRepository.authenticate(this, identifier, password);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (!authenticated) {
                        errorText.setText(R.string.auth_credentials_invalid);
                        errorText.setVisibility(View.VISIBLE);
                        loginButton.setEnabled(true);
                        loginButton.setAlpha(1f);
                        loginButton.setText(R.string.login_button);
                        return;
                    }
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
            }, "moc-mien-login").start();
        });

        Button facebookLoginButton = findViewById(R.id.facebookLoginButton);
        facebookLoginButton.setOnClickListener(view -> showSocialSignInNotice("Facebook"));

        Button googleLoginButton = findViewById(R.id.googleLoginButton);
        googleLoginButton.setOnClickListener(view -> showSocialSignInNotice("Google"));

        findViewById(R.id.registerAction).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void showSocialSignInNotice(String provider) {
        Toast.makeText(this, getString(R.string.social_login_unavailable, provider), Toast.LENGTH_SHORT).show();
    }
}
