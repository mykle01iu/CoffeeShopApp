package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.AuthRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class ChangePasswordActivity extends Activity {

    private EditText currentInput, newInput, confirmInput;
    private TextView currentError, newError, confirmError, strengthText;
    private View strengthContainer;
    private ProgressBar strengthBar, loadingBar;
    private Button updateButton;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        findViewById(R.id.changePasswordBack).setOnClickListener(v -> finish());

        currentInput = findViewById(R.id.currentPasswordInput);
        newInput = findViewById(R.id.newPasswordInput);
        confirmInput = findViewById(R.id.confirmPasswordInput);

        currentError = findViewById(R.id.currentPasswordError);
        newError = findViewById(R.id.newPasswordError);
        confirmError = findViewById(R.id.confirmPasswordError);

        strengthContainer = findViewById(R.id.strengthContainer);
        strengthBar = findViewById(R.id.strengthProgressBar);
        strengthText = findViewById(R.id.strengthText);

        updateButton = findViewById(R.id.updatePasswordButton);
        loadingBar = findViewById(R.id.loadingProgressBar);

        ImageButton currentToggle = findViewById(R.id.currentPasswordToggle);
        ImageButton newToggle = findViewById(R.id.newPasswordToggle);
        ImageButton confirmToggle = findViewById(R.id.confirmPasswordToggle);

        currentToggle.setOnClickListener(v -> {
            isCurrentVisible = !isCurrentVisible;
            toggleVisibility(currentInput, currentToggle, isCurrentVisible);
        });

        newToggle.setOnClickListener(v -> {
            isNewVisible = !isNewVisible;
            toggleVisibility(newInput, newToggle, isNewVisible);
        });

        confirmToggle.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            toggleVisibility(confirmInput, confirmToggle, isConfirmVisible);
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        currentInput.addTextChangedListener(watcher);
        newInput.addTextChangedListener(watcher);
        confirmInput.addTextChangedListener(watcher);

        updateButton.setOnClickListener(v -> performChangePassword());
    }

    private void toggleVisibility(EditText editText, ImageButton button, boolean isVisible) {
        int selection = editText.getSelectionEnd();
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setImageResource(R.drawable.ic_visibility_off);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            button.setImageResource(R.drawable.ic_visibility);
        }
        editText.setSelection(selection);
    }

    private void validateForm() {
        String currentPass = currentInput.getText().toString();
        String newPass = newInput.getText().toString();
        String confirmPass = confirmInput.getText().toString();

        boolean isValid = true;

        currentError.setVisibility(View.GONE);
        newError.setVisibility(View.GONE);
        confirmError.setVisibility(View.GONE);

        if (currentPass.isEmpty()) {
            isValid = false;
        }

        if (newPass.isEmpty()) {
            isValid = false;
            strengthContainer.setVisibility(View.GONE);
        } else {
            strengthContainer.setVisibility(View.VISIBLE);
            updateStrengthIndicator(newPass);

            if (newPass.length() < 8 || !newPass.matches(".*[A-Za-z].*") || !newPass.matches(".*[0-9].*")) {
                newError.setText("Mật khẩu mới cần tối thiểu 8 ký tự, có cả chữ và số.");
                newError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!currentPass.isEmpty() && newPass.equals(currentPass)) {
                newError.setText("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
                newError.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        if (confirmPass.isEmpty()) {
            isValid = false;
        } else if (!confirmPass.equals(newPass)) {
            confirmError.setText("Mật khẩu xác nhận không khớp.");
            confirmError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        updateButton.setEnabled(isValid);
    }

    private void updateStrengthIndicator(String password) {
        int score = 0;
        if (password.length() >= 8) score += 30;
        if (password.length() >= 12) score += 20;
        if (password.matches(".*[A-Z].*")) score += 20;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 15;

        strengthBar.setProgress(score);

        if (score < 50) {
            strengthText.setText("Yếu");
            strengthText.setTextColor(Color.parseColor("#E53935"));
        } else if (score < 80) {
            strengthText.setText("Trung bình");
            strengthText.setTextColor(Color.parseColor("#FB8C00"));
        } else {
            strengthText.setText("Mạnh");
            strengthText.setTextColor(Color.parseColor("#43A047"));
        }
    }

    private void performChangePassword() {
        String currentPass = currentInput.getText().toString();
        String newPass = newInput.getText().toString();

        updateButton.setEnabled(false);
        updateButton.setText("");
        loadingBar.setVisibility(View.VISIBLE);

        AuthRepository.changePassword(this, currentPass, newPass, new AuthRepository.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                clearPasswords();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                loadingBar.setVisibility(View.GONE);
                updateButton.setText(R.string.update_password_button);
                updateButton.setEnabled(true);
                currentError.setText(errorMessage);
                currentError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void clearPasswords() {
        if (currentInput != null) currentInput.setText("");
        if (newInput != null) newInput.setText("");
        if (confirmInput != null) confirmInput.setText("");
    }

    @Override
    protected void onDestroy() {

        clearPasswords();
        super.onDestroy();
    }
}
