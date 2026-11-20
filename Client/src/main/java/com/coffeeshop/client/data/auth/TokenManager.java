package com.coffeeshop.client.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public final class TokenManager {
    private static final String FILE_NAME = "moc_mien_secure_session";
    private static volatile TokenManager instance;

    private final SharedPreferences preferences;
    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();

    private TokenManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            preferences = EncryptedSharedPreferences.create(
                    FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            loggedIn.setValue(!getToken().isEmpty());
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể khởi tạo bộ nhớ phiên đăng nhập an toàn.", exception);
        }
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public synchronized void saveSession(String token, String name, String email) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token đăng nhập không hợp lệ.");
        }
        boolean saved = preferences.edit()
                .putString("token", token)
                .putString("name", name == null ? "" : name)
                .putString("email", email == null ? "" : email)
                .commit();
        if (!saved) throw new IllegalStateException("Không lưu được phiên đăng nhập.");
        loggedIn.postValue(true);
    }

    public synchronized String getToken() {
        return preferences.getString("token", "");
    }

    public String getName() {
        return preferences.getString("name", "");
    }

    public String getEmail() {
        return preferences.getString("email", "");
    }

    public synchronized void clear() {
        preferences.edit().clear().commit();
        loggedIn.postValue(false);
    }

    public LiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }
}
