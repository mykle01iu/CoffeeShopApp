package com.coffeeshop.client.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
        ((TextView) findViewById(R.id.loginFormTitle)).setTypeface(FontUtils.lora(this, 600));

        EditText identifierInput = findViewById(R.id.emailInput);
        String savedIdentifier = getIntent().getStringExtra(LocalAuthRepository.EXTRA_IDENTIFIER);
        if (savedIdentifier != null) identifierInput.setText(savedIdentifier);
        EditText passwordInput = findViewById(R.id.passwordInput);
        TextView emailError = findViewById(R.id.emailErrorText);
        TextView passwordError = findViewById(R.id.passwordErrorText);
        ImageButton passwordVisibilityButton = findViewById(R.id.passwordVisibilityButton);
        Button loginButton = findViewById(R.id.loginButton);
        ProgressBar loginProgress = findViewById(R.id.loginProgress);
        View loginArrow = findViewById(R.id.loginArrowIcon);

        findViewById(R.id.loginBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.forgotPasswordAction).setOnClickListener(view ->
                Toast.makeText(this, R.string.password_reset_unavailable, Toast.LENGTH_SHORT).show());

        addErrorClearingWatcher(identifierInput, emailError);
        addErrorClearingWatcher(passwordInput, passwordError);

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
            passwordInput.requestFocus();
        });

        loginButton.setOnClickListener(view -> {
            String identifier = identifierInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            clearError(emailError);
            clearError(passwordError);
            if (identifier.isEmpty()) {
                showError(emailError, R.string.login_error_identifier_required);
                identifierInput.requestFocus();
                return;
            }
            if (!AuthValidation.isValidIdentifier(identifier)) {
                showError(emailError, R.string.auth_identifier_invalid);
                identifierInput.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                showError(passwordError, R.string.login_error_password_required);
                passwordInput.requestFocus();
                return;
            }
            if (!AuthValidation.isStrongEnoughPassword(password)) {
                showError(passwordError, R.string.auth_password_rule);
                passwordInput.requestFocus();
                return;
            }

            hideKeyboard(passwordInput);
            loginButton.setEnabled(false);
            loginButton.setAlpha(0.72f);
            loginButton.setText(R.string.login_processing);
            loginArrow.setVisibility(View.GONE);
            loginProgress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    boolean authenticated = LocalAuthRepository.authenticate(this, identifier, password);
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (!authenticated) {
                            showError(passwordError, R.string.auth_credentials_invalid);
                            loginButton.setEnabled(true);
                            loginButton.setAlpha(1f);
                            loginButton.setText(R.string.login_button);
                            loginArrow.setVisibility(View.VISIBLE);
                            loginProgress.setVisibility(View.GONE);
                            return;
                        }
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    });
                } catch (Exception exception) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        showError(passwordError, R.string.auth_credentials_invalid);
                        loginButton.setEnabled(true);
                        loginButton.setAlpha(1f);
                        loginButton.setText(R.string.login_button);
                        loginArrow.setVisibility(View.VISIBLE);
                        loginProgress.setVisibility(View.GONE);
                    });
                }
            }, "moc-mien-login").start();
        });

        passwordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick();
                return true;
            }
            return false;
        });

        findViewById(R.id.facebookLoginButton).setOnClickListener(view -> showSocialSignInNotice("Facebook"));
        findViewById(R.id.googleLoginButton).setOnClickListener(view -> showSocialSignInNotice("Google"));
        findViewById(R.id.registerAction).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void addErrorClearingWatcher(EditText input, TextView error) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) clearError(error);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void showError(TextView error, int messageId) {
        error.setText(messageId);
        error.setVisibility(View.VISIBLE);
    }

    private void clearError(TextView error) {
        error.setText("");
        error.setVisibility(View.GONE);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSocialSignInNotice(String provider) {
        Toast.makeText(this, getString(R.string.social_login_unavailable, provider), Toast.LENGTH_SHORT).show();
    }
}
