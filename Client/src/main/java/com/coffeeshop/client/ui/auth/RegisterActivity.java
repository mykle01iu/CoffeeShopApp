package com.coffeeshop.client.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.coffeeshop.client.ui.home.HomeActivity;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.registerTitle)).setTypeface(FontUtils.lora(this, 600));

        EditText nameInput = findViewById(R.id.nameInput);
        EditText identifierInput = findViewById(R.id.registerEmailInput);
        EditText passwordInput = findViewById(R.id.registerPasswordInput);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        TextView nameError = findViewById(R.id.nameErrorText);
        TextView emailError = findViewById(R.id.emailErrorText);
        TextView passwordError = findViewById(R.id.passwordErrorText);
        TextView confirmPasswordError = findViewById(R.id.confirmPasswordErrorText);
        TextView generalError = findViewById(R.id.registerGeneralErrorText);

        ImageButton passwordVisibilityButton = findViewById(R.id.registerPasswordVisibilityButton);
        ImageButton confirmPasswordVisibilityButton = findViewById(R.id.confirmPasswordVisibilityButton);
        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox);
        Button registerButton = findViewById(R.id.registerButton);
        ProgressBar registerProgress = findViewById(R.id.registerProgress);
        View registerArrow = findViewById(R.id.registerArrowIcon);

        findViewById(R.id.registerBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.loginAction).setOnClickListener(view -> finish());
        findViewById(R.id.termsLegalAction).setOnClickListener(view ->
                Toast.makeText(this, "Điều khoản sử dụng và Chính sách bảo mật của Mộc Miên Coffee", Toast.LENGTH_SHORT).show());

        addErrorClearingWatcher(nameInput, nameError);
        addErrorClearingWatcher(identifierInput, emailError);
        addErrorClearingWatcher(passwordInput, passwordError);
        addErrorClearingWatcher(confirmPasswordInput, confirmPasswordError);

        // Initial terms checkbox state lock
        registerButton.setEnabled(false);
        registerButton.setAlpha(0.5f);

        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            clearError(generalError);
            if (isChecked) {
                registerButton.setEnabled(true);
                registerButton.setAlpha(1.0f);
            } else {
                registerButton.setEnabled(false);
                registerButton.setAlpha(0.5f);
            }
        });

        setupPasswordToggle(passwordInput, passwordVisibilityButton);
        setupPasswordToggle(confirmPasswordInput, confirmPasswordVisibilityButton);

        confirmPasswordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (registerButton.isEnabled()) {
                    registerButton.performClick();
                }
                return true;
            }
            return false;
        });

        registerButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString().trim();
            String identifier = identifierInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            clearError(nameError);
            clearError(emailError);
            clearError(passwordError);
            clearError(confirmPasswordError);
            clearError(generalError);

            if (name.isEmpty()) {
                showError(nameError, R.string.register_error_name_required);
                nameInput.requestFocus();
                return;
            }
            if (identifier.isEmpty()) {
                showError(emailError, R.string.auth_identifier_required);
                identifierInput.requestFocus();
                return;
            }
            if (!AuthValidation.isValidIdentifier(identifier)) {
                showError(emailError, R.string.auth_identifier_invalid);
                identifierInput.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                showError(passwordError, R.string.register_error_password_required);
                passwordInput.requestFocus();
                return;
            }
            if (!AuthValidation.isStrongEnoughPassword(password)) {
                showError(passwordError, R.string.auth_password_rule);
                passwordInput.requestFocus();
                return;
            }
            if (!password.equals(confirmPassword)) {
                showErrorDirect(confirmPasswordError, "Mật khẩu xác nhận không trùng khớp.");
                confirmPasswordInput.requestFocus();
                return;
            }
            if (!termsCheckBox.isChecked()) {
                showError(generalError, R.string.register_error_terms_required);
                termsCheckBox.requestFocus();
                return;
            }

            hideKeyboard(confirmPasswordInput);
            registerButton.setEnabled(false);
            registerButton.setAlpha(0.72f);
            registerButton.setText(R.string.register_processing);
            registerArrow.setVisibility(View.GONE);
            registerProgress.setVisibility(View.VISIBLE);

            new Thread(() -> {
                try {
                    boolean registered = LocalAuthRepository.register(this, name, identifier, password);
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (!registered) {
                            showError(generalError, R.string.auth_account_exists);
                            registerButton.setEnabled(true);
                            registerButton.setAlpha(1.0f);
                            registerButton.setText(R.string.register_create_button);
                            registerArrow.setVisibility(View.VISIBLE);
                            registerProgress.setVisibility(View.GONE);
                            return;
                        }
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    });
                } catch (Exception exception) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        showError(generalError, R.string.auth_account_exists);
                        registerButton.setEnabled(true);
                        registerButton.setAlpha(1.0f);
                        registerButton.setText(R.string.register_create_button);
                        registerArrow.setVisibility(View.VISIBLE);
                        registerProgress.setVisibility(View.GONE);
                    });
                }
            }, "moc-mien-register").start();
        });
    }

    private void setupPasswordToggle(EditText input, ImageButton toggleButton) {
        toggleButton.setOnClickListener(view -> {
            int cursorPosition = input.getSelectionStart();
            boolean showingPassword = input.getTransformationMethod()
                    instanceof HideReturnsTransformationMethod;
            if (showingPassword) {
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleButton.setImageResource(R.drawable.ic_visibility);
                toggleButton.setContentDescription(getString(R.string.show_password));
            } else {
                input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleButton.setImageResource(R.drawable.ic_visibility_off);
                toggleButton.setContentDescription(getString(R.string.hide_password));
            }
            if (cursorPosition >= 0) input.setSelection(cursorPosition);
            input.requestFocus();
        });
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

    private void showErrorDirect(TextView error, String message) {
        error.setText(message);
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
}
