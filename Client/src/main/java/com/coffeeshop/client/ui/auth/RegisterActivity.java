package com.coffeeshop.client.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;

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
        ((TextView) findViewById(R.id.registerTitle)).setTypeface(FontUtils.lora(this, 400));
        ((TextView) findViewById(R.id.registerBrandText)).setTypeface(FontUtils.lora(this, 600));

        EditText nameInput = findViewById(R.id.nameInput);
        EditText identifierInput = findViewById(R.id.registerEmailInput);
        EditText passwordInput = findViewById(R.id.registerPasswordInput);
        ImageButton passwordVisibilityButton = findViewById(R.id.registerPasswordVisibilityButton);
        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox);
        TextView errorText = findViewById(R.id.registerErrorText);

        findViewById(R.id.registerBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.loginAction).setOnClickListener(view -> finish());

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

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> {
            if (nameInput.getText().toString().trim().isEmpty()) {
                errorText.setText(R.string.register_error_name_required);
                errorText.setVisibility(View.VISIBLE);
                nameInput.requestFocus();
                return;
            }
            String name = nameInput.getText().toString().trim();
            String identifier = identifierInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            if (identifier.isEmpty()) {
                errorText.setText(R.string.auth_identifier_required);
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
                errorText.setText(R.string.register_error_password_required);
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
            if (!termsCheckBox.isChecked()) {
                errorText.setText(R.string.register_error_terms_required);
                errorText.setVisibility(View.VISIBLE);
                termsCheckBox.requestFocus();
                return;
            }

            errorText.setVisibility(View.GONE);
            registerButton.setEnabled(false);
            registerButton.setAlpha(0.65f);
            registerButton.setText(R.string.register_processing);
            new Thread(() -> {
                boolean registered = LocalAuthRepository.register(this, name, identifier, password);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (!registered) {
                        errorText.setText(R.string.auth_account_exists);
                        errorText.setVisibility(View.VISIBLE);
                        registerButton.setEnabled(true);
                        registerButton.setAlpha(1f);
                        registerButton.setText(R.string.register_create_button);
                        return;
                    }
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
            }, "moc-mien-register").start();
        });
    }
}
