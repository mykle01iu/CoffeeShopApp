package com.example.myapplication.ui;

import android.app.Activity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.auth.AccountStore;

public class LoginScreen {

    private final Activity activity;
    private final Navigator navigator;
    private final AccountStore accounts;

    private TextView authTitle, authSubtitle, loginError, registerError, authSwitchLink;
    private View loginContainer, registerContainer;
    private boolean showingLogin = true;
    private boolean showingChoice = true;

    public LoginScreen(Activity activity, Navigator navigator, AccountStore accounts) {
        this.activity = activity;
        this.navigator = navigator;
        this.accounts = accounts;
    }

    public void show() {
        showingChoice = true;
        activity.setContentView(R.layout.auth_choice);
        activity.findViewById(R.id.choice_login).setOnClickListener(v -> showForm(true));
        activity.findViewById(R.id.choice_register).setOnClickListener(v -> showForm(false));
    }

    private void showForm(boolean isLogin) {
        showingChoice = false;
        activity.setContentView(R.layout.login);

        authSwitchLink = activity.findViewById(R.id.auth_switch_link);
        loginContainer = activity.findViewById(R.id.login_container);
        registerContainer = activity.findViewById(R.id.register_container);
        authTitle = activity.findViewById(R.id.auth_title);
        authSubtitle = activity.findViewById(R.id.auth_subtitle);
        loginError = activity.findViewById(R.id.login_error);
        registerError = activity.findViewById(R.id.register_error);

        authSwitchLink.setOnClickListener(v -> showAuthTab(!showingLogin));
        activity.findViewById(R.id.login_button).setOnClickListener(v -> onLogin());
        activity.findViewById(R.id.register_button).setOnClickListener(v -> onRegister());
        activity.findViewById(R.id.forgot_password).setOnClickListener(v ->
                Toast.makeText(activity, "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu", Toast.LENGTH_SHORT).show());

        showAuthTab(isLogin);
    }

    private void showAuthTab(boolean isLogin) {
        showingLogin = isLogin;
        loginContainer.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        registerContainer.setVisibility(isLogin ? View.GONE : View.VISIBLE);

        authTitle.setText(isLogin ? "Chào mừng trở lại" : "Đăng ký tài khoản");
        authSubtitle.setText(isLogin
                ? "Đăng nhập để quản lý cửa hàng của bạn"
                : "Tạo tài khoản để quản lý cửa hàng");

        loginError.setVisibility(View.GONE);
        registerError.setVisibility(View.GONE);

        authSwitchLink.setText(isLogin ? "Đăng ký ngay" : "Đã có tài khoản? Đăng nhập");
    }

    private void onLogin() {
        String email = UiUtils.textOf(edit(R.id.email_input));
        String password = UiUtils.rawTextOf(edit(R.id.password_input));

        if (email.isEmpty() || password.isEmpty()) {
            UiUtils.showError(loginError, "Vui lòng nhập email và mật khẩu.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UiUtils.showError(loginError, "Email không hợp lệ.");
            return;
        }

        switch (accounts.login(email, password)) {
            case SUCCESS:
                navigator.showDashboard();
                break;
            case PENDING_APPROVAL:
                UiUtils.showError(loginError, "Tài khoản đang chờ admin phê duyệt.");
                break;
            case WRONG_CREDENTIALS:
                UiUtils.showError(loginError, "Email hoặc mật khẩu không chính xác.");
                break;
            default:
                UiUtils.showError(loginError, "Không thể xác thực tài khoản. Vui lòng thử lại.");
                break;
        }
    }

    private void onRegister() {
        String name = UiUtils.textOf(edit(R.id.name_input));
        String phone = UiUtils.textOf(edit(R.id.phone_input));
        String email = UiUtils.textOf(edit(R.id.reg_email_input));
        String password = UiUtils.rawTextOf(edit(R.id.reg_password_input));
        String confirm = UiUtils.rawTextOf(edit(R.id.confirm_password_input));

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            UiUtils.showError(registerError, "Vui lòng điền đầy đủ thông tin.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UiUtils.showError(registerError, "Email không hợp lệ.");
            return;
        }
        if (password.length() < 8) {
            UiUtils.showError(registerError, "Mật khẩu phải có ít nhất 8 ký tự.");
            return;
        }
        if (!password.equals(confirm)) {
            UiUtils.showError(registerError, "Mật khẩu xác nhận không khớp.");
            return;
        }

        switch (accounts.register(name, phone, email, password)) {
            case ADMIN_CREATED:
                finishRegistration(email);
                Toast.makeText(activity, "Đã tạo tài khoản quản trị. Bạn có thể đăng nhập ngay.", Toast.LENGTH_LONG).show();
                break;
            case PENDING_APPROVAL:
                finishRegistration(email);
                Toast.makeText(activity, "Đã gửi đăng ký. Vui lòng chờ admin phê duyệt tài khoản.", Toast.LENGTH_LONG).show();
                break;
            case ALREADY_EXISTS:
                UiUtils.showError(registerError, "Email này đã được đăng ký.");
                break;
            default:
                UiUtils.showError(registerError, "Chưa thể lưu tài khoản. Vui lòng thử lại.");
                break;
        }
    }

    private void finishRegistration(String email) {
        edit(R.id.name_input).setText("");
        edit(R.id.phone_input).setText("");
        edit(R.id.reg_email_input).setText("");
        edit(R.id.reg_password_input).setText("");
        edit(R.id.confirm_password_input).setText("");

        edit(R.id.email_input).setText(email);
        edit(R.id.password_input).setText("");
        showAuthTab(true);
    }

    private EditText edit(int id) {
        return activity.findViewById(id);
    }
}
