package com.coffeeshop.client.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private TextView nameError;
    private TextView emailError;
    private TextView passwordError;
    private TextView confirmError;
    private TextView generalError;
    private Button registerButton;
    private ProgressBar progress;
    private View arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.registerTitle)).setTypeface(FontUtils.lora(this, 600));

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        RadioGroup methodGroup = findViewById(R.id.registerMethodGroup);
        methodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean phone = checkedId == R.id.registerPhoneMethod;
            emailInput.setHint(phone ? "09xxxxxxxx" : "ten@email.com");
            emailInput.setInputType(phone
                    ? android.text.InputType.TYPE_CLASS_PHONE
                    : android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            clearError(emailError);
        });
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmInput = findViewById(R.id.confirmPasswordInput);
        nameError = findViewById(R.id.nameErrorText);
        emailError = findViewById(R.id.emailErrorText);
        passwordError = findViewById(R.id.passwordErrorText);
        confirmError = findViewById(R.id.confirmPasswordErrorText);
        generalError = findViewById(R.id.registerGeneralErrorText);
        CheckBox terms = findViewById(R.id.termsCheckBox);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.registerProgress);
        arrow = findViewById(R.id.registerArrowIcon);

        findViewById(R.id.registerBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.loginAction).setOnClickListener(view -> finish());
        findViewById(R.id.termsLegalAction).setOnClickListener(view ->
                Toast.makeText(this,
                        "Điều khoản sử dụng và Chính sách bảo mật của Mộc Miên Coffee",
                        Toast.LENGTH_SHORT).show());
        addErrorWatcher(nameInput, nameError);
        addErrorWatcher(emailInput, emailError);
        addErrorWatcher(passwordInput, passwordError);
        addErrorWatcher(confirmInput, confirmError);
        setupPasswordToggle(passwordInput, (ImageButton) findViewById(R.id.registerPasswordVisibilityButton));
        setupPasswordToggle(confirmInput, (ImageButton) findViewById(R.id.confirmPasswordVisibilityButton));

        registerButton.setEnabled(false);
        registerButton.setAlpha(0.5f);
        terms.setOnCheckedChangeListener((button, checked) -> {
            registerButton.setEnabled(checked);
            registerButton.setAlpha(checked ? 1f : 0.5f);
            if (checked) clearError(generalError);
        });
        registerButton.setOnClickListener(view -> submitRegistration(terms));

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.getAuthState().observe(this, this::renderAuthState);
    }

    private void submitRegistration(CheckBox terms) {
        clearError(nameError);
        clearError(emailError);
        clearError(passwordError);
        clearError(confirmError);
        clearError(generalError);
        String name = nameInput.getText().toString().trim();
        String identifier = emailInput.getText().toString().trim();
        boolean phoneRegistration = ((RadioGroup) findViewById(R.id.registerMethodGroup))
                .getCheckedRadioButtonId() == R.id.registerPhoneMethod;
        String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();
        if (name.isEmpty()) {
            showError(nameError, "Vui lòng nhập họ và tên.");
            nameInput.requestFocus();
            return;
        }
        if (identifier.isEmpty()) {
            showError(emailError, getString(phoneRegistration ? R.string.register_phone_required : R.string.register_email_required));
            emailInput.requestFocus();
            return;
        }
        if (phoneRegistration ? !AuthValidation.isValidIdentifier(identifier) || identifier.contains("@")
                : !AuthValidation.isValidEmail(identifier)) {
            showError(emailError, getString(phoneRegistration ? R.string.register_phone_invalid : R.string.register_email_invalid));
            emailInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showError(passwordError, "Mật khẩu cần tối thiểu 6 ký tự.");
            passwordInput.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            showError(confirmError, "Mật khẩu xác nhận không trùng khớp.");
            confirmInput.requestFocus();
            return;
        }
        if (!terms.isChecked()) {
            showError(generalError, getString(R.string.register_error_terms_required));
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) inputManager.hideSoftInputFromWindow(confirmInput.getWindowToken(), 0);
        viewModel.register(name, identifier, password, phoneRegistration);
    }

    private void renderAuthState(ApiResult<AuthResponseDto> result) {
        if (result == null) return;
        boolean loading = result.status == ApiResult.Status.LOADING;
        registerButton.setEnabled(!loading);
        registerButton.setAlpha(loading ? 0.72f : 1f);
        registerButton.setText(loading ? R.string.register_processing : R.string.register_create_button);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        arrow.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (result.status == ApiResult.Status.SUCCESS) {
            startActivity(new Intent(this, MenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else if (result.status == ApiResult.Status.ERROR) {
            showError(generalError, result.message);
        }
    }

    private void setupPasswordToggle(EditText input, ImageButton button) {
        button.setOnClickListener(view -> {
            int cursor = input.getSelectionStart();
            boolean visible = input.getTransformationMethod() instanceof HideReturnsTransformationMethod;
            input.setTransformationMethod(visible
                    ? PasswordTransformationMethod.getInstance()
                    : HideReturnsTransformationMethod.getInstance());
            button.setImageResource(visible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
            button.setContentDescription(getString(visible ? R.string.show_password : R.string.hide_password));
            if (cursor >= 0) input.setSelection(cursor);
        });
    }

    private void addErrorWatcher(EditText input, TextView error) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) clearError(error);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void showError(TextView view, String message) {
        view.setText(message);
        view.setVisibility(View.VISIBLE);
    }

    private void clearError(TextView view) {
        view.setText("");
        view.setVisibility(View.GONE);
    }
}
