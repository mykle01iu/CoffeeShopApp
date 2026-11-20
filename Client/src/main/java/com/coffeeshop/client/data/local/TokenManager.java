package com.coffeeshop.client.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class TokenManager {
    private static final String PREF_NAME = "encrypted_auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";

    private static SharedPreferences sharedPreferences;
    private static final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>(false);

    private static synchronized SharedPreferences getPrefs(Context context) {
        if (sharedPreferences == null) {
            try {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                sharedPreferences = EncryptedSharedPreferences.create(
                        PREF_NAME,
                        masterKeyAlias,
                        context.getApplicationContext(),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (Exception e) {
                sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            }
        }
        return sharedPreferences;
    }

    public static synchronized void saveSession(Context context, String token, String name, String email) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .putString(KEY_TOKEN, token != null ? token : "")
                .putString(KEY_NAME, name != null ? name : "")
                .putString(KEY_EMAIL, email != null ? email : "")
                .apply();
        isLoggedInLiveData.postValue(token != null && !token.isEmpty());
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
    }

    public static String getUserName(Context context) {
        return getPrefs(context).getString(KEY_NAME, "");
    }

    public static String getUserEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    public static boolean isLoggedIn(Context context) {
        String token = getToken(context);
        boolean loggedIn = token != null && !token.isEmpty();
        isLoggedInLiveData.postValue(loggedIn);
        return loggedIn;
    }

    public static LiveData<Boolean> getLoggedInLiveData(Context context) {
        isLoggedIn(context);
        return isLoggedInLiveData;
    }

    public static synchronized void clear(Context context) {
        getPrefs(context).edit().clear().apply();
        isLoggedInLiveData.postValue(false);
    }
}
