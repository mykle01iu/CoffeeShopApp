package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the authenticated backend session. */
public class SessionManager {

    private static final String PREFS_NAME = "auth_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_BACKEND_SESSION = "backend_session";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Legacy local-session method retained for AccountStore compatibility. */
    public void createSession(String email, String role) {
        prefs.edit()
                .remove(KEY_TOKEN)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putBoolean(KEY_BACKEND_SESSION, false)
                .commit();
    }

    public void createBackendSession(String token, String email, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putBoolean(KEY_BACKEND_SESSION, true)
                .commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_BACKEND_SESSION, false) && getToken() != null;
    }

    public boolean isAdmin() {
        return AccountStore.ROLE_ADMIN.equalsIgnoreCase(getRole());
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
