package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

// Quản lý phiên đăng nhập. Token hiện là placeholder (UUID ngẫu nhiên),
// sau này có backend thì thay bằng token thật từ server.
public class SessionManager {

    private static final String PREFS_NAME = "auth_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void createSession(String email, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, UUID.randomUUID().toString())
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .commit();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public boolean isAdmin() {
        return AccountStore.ROLE_ADMIN.equals(getRole());
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public void clear() {
        prefs.edit().clear().commit();
    }
}