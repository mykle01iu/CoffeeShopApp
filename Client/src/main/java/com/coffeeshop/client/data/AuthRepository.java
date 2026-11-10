package com.coffeeshop.client.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.coffeeshop.client.ui.auth.LocalAuthRepository;

public final class AuthRepository {

    public interface Callback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private AuthRepository() { }

    public static void changePassword(Context context, String oldPassword, String newPassword, Callback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean valid = LocalAuthRepository.checkPassword(context, oldPassword);
            if (!valid) {
                if (callback != null) callback.onError("Mật khẩu hiện tại không đúng");
                return;
            }
            LocalAuthRepository.setAccountPassword(context, newPassword);
            if (callback != null) callback.onSuccess();
        }, 1000);
    }
}
