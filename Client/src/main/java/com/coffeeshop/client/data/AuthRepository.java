package com.coffeeshop.client.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.coffeeshop.client.data.auth.TokenManager;
import com.coffeeshop.client.data.remote.ApiExecutor;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.ApiService;
import com.coffeeshop.client.data.remote.RetrofitClient;
import com.coffeeshop.client.data.remote.dto.AuthResponseDto;
import com.coffeeshop.client.data.remote.dto.LoginRequestDto;
import com.coffeeshop.client.data.remote.dto.RegisterRequestDto;
import com.coffeeshop.client.ui.auth.LocalAuthRepository;

public final class AuthRepository {
    public interface Callback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private final ApiService api;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        api = RetrofitClient.getApiService(context);
        tokenManager = TokenManager.getInstance(context);
    }

    public ApiResult<AuthResponseDto> login(String identifier, String password) {
        ApiResult<AuthResponseDto> result = ApiExecutor.safeApiCall(
                api.login(new LoginRequestDto(identifier, password))
        );
        if (result.status == ApiResult.Status.SUCCESS && result.data != null) {
            try {
                saveSession(result.data);
            } catch (Exception exception) {
                return ApiResult.error(exception.getMessage());
            }
        }
        return result;
    }

    public ApiResult<AuthResponseDto> register(String name, String email, String password) {
        return register(name, email, password, false);
    }

    public ApiResult<AuthResponseDto> register(String name, String identifier, String password, boolean phone) {
        ApiResult<AuthResponseDto> registered = ApiExecutor.safeApiCall(
                api.register(new RegisterRequestDto(name, identifier, password, phone))
        );
        if (registered.status == ApiResult.Status.ERROR) {
            return ApiResult.error(registered.message);
        }
        if (registered.status == ApiResult.Status.SUCCESS && registered.data != null && registered.data.getToken() != null) {
            try {
                saveSession(registered.data);
                return registered;
            } catch (Exception ignored) { }
        }

        // Tự động đăng nhập ngay sau khi tạo tài khoản nếu server không trả token trực tiếp
        ApiResult<AuthResponseDto> loginResult = ApiExecutor.safeApiCall(
                api.login(new LoginRequestDto(identifier, password))
        );
        if (loginResult.status == ApiResult.Status.SUCCESS && loginResult.data != null) {
            try {
                saveSession(loginResult.data);
            } catch (Exception exception) {
                return ApiResult.error(exception.getMessage());
            }
        }
        return loginResult;
    }

    public void logout() {
        tokenManager.clear();
    }

    /** Backend hiện chưa có endpoint đổi mật khẩu; giữ luồng cục bộ cũ để màn cũ còn dùng được. */
    public static void changePassword(Context context, String currentPassword,
                                      String newPassword, Callback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                if (!LocalAuthRepository.checkPassword(context, currentPassword)) {
                    mainHandler.post(() -> callback.onError("Mật khẩu hiện tại không chính xác."));
                    return;
                }
                LocalAuthRepository.setAccountPassword(context, newPassword);
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onError("Không thể đổi mật khẩu lúc này."));
            }
        }, "moc-mien-change-password").start();
    }

    private void saveSession(AuthResponseDto response) {
        if (response == null || response.getToken() == null || response.getToken().trim().isEmpty()) {
            throw new IllegalStateException("Máy chủ đăng nhập không trả token.");
        }
        String phone = response.getPhone();
        String identifier = phone != null && !phone.trim().isEmpty()
                ? phone : response.getEmail();
        tokenManager.saveSession(response.getToken(), response.getName(), identifier);
    }
}
