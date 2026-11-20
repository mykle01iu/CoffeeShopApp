package com.coffeeshop.client.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.AuthResponse;
import com.coffeeshop.client.data.remote.dto.AuthResponseDto;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.coffeeshop.client.ui.home.MenuActivity;

public class LoginActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView emailError;
    private TextView passwordError;
    private Button loginButton;
    private ProgressBar progress;
    private View arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.loginFormTitle)).setTypeface(FontUtils.lora(this, 600));

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailError = findViewById(R.id.emailErrorText);
        passwordError = findViewById(R.id.passwordErrorText);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.loginProgress);
        arrow = findViewById(R.id.loginArrowIcon);
        ImageButton visibility = findViewById(R.id.passwordVisibilityButton);

        findViewById(R.id.loginBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.forgotPasswordAction).setOnClickListener(view ->
                Toast.makeText(this, R.string.password_reset_unavailable, Toast.LENGTH_SHORT).show());
        findViewById(R.id.registerAction).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));

        emailInput.addTextChangedListener(clearErrorWatcher(emailError));
        passwordInput.addTextChangedListener(clearErrorWatcher(passwordError));
        visibility.setOnClickListener(view -> togglePassword(passwordInput, visibility));
        loginButton.setOnClickListener(view -> submitLogin());
        passwordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick();
                return true;
            }
            return false;
        });
        findViewById(R.id.facebookLoginButton).setOnClickListener(view -> showSocialNotice("Facebook"));
        findViewById(R.id.googleLoginButton).setOnClickListener(view -> showSocialNotice("Google"));

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.getAuthState().observe(this, this::renderAuthState);
    }

    private void submitLogin() {
        clearError(emailError);
        clearError(passwordError);
        String identifier = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (identifier.isEmpty()) {
            showError(emailError, getString(R.string.login_error_identifier_required));
            emailInput.requestFocus();
            return;
        }
        if (!AuthValidation.isValidIdentifier(identifier)) {
            showError(emailError, "Vui lòng nhập email đúng định dạng.");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError(passwordError, getString(R.string.login_error_password_required));
            passwordInput.requestFocus();
            return;
        }
        viewModel.login(identifier, password);
    }

    private void renderAuthState(ApiResult<AuthResponseDto> result) {
        if (result == null) return;
        boolean loading = result.status == ApiResult.Status.LOADING;
        loginButton.setEnabled(!loading);
        loginButton.setAlpha(loading ? 0.72f : 1f);
        loginButton.setText(loading ? R.string.login_processing : R.string.login_button);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        arrow.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (result.status == ApiResult.Status.SUCCESS) {
            startActivity(new Intent(this, MenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else if (result.status == ApiResult.Status.ERROR) {
            showError(passwordError, result.message);
        }
    }

    private TextWatcher clearErrorWatcher(TextView error) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) clearError(error);
            }
            @Override public void afterTextChanged(Editable s) { }
        };
    }

    private void togglePassword(EditText input, ImageButton button) {
        int cursor = input.getSelectionStart();
        boolean visible = input.getTransformationMethod() instanceof HideReturnsTransformationMethod;
        input.setTransformationMethod(visible
                ? PasswordTransformationMethod.getInstance()
                : HideReturnsTransformationMethod.getInstance());
        button.setImageResource(visible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        button.setContentDescription(getString(visible ? R.string.show_password : R.string.hide_password));
        if (cursor >= 0) input.setSelection(cursor);
    }

    private void showError(TextView view, String message) {
        view.setText(message);
        view.setVisibility(View.VISIBLE);
    }

    private void clearError(TextView view) {
        view.setText("");
        view.setVisibility(View.GONE);
    }

    private void showSocialNotice(String provider) {
        Toast.makeText(this, getString(R.string.social_login_unavailable, provider), Toast.LENGTH_SHORT).show();
    }
}
