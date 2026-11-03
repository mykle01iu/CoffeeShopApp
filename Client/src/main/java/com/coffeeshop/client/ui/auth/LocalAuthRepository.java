package com.coffeeshop.client.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Device-local prototype auth store. Replace with the app's real auth API when available. */
public final class LocalAuthRepository {
    public static final String EXTRA_IDENTIFIER = "com.coffeeshop.client.auth.IDENTIFIER";
    private static final String ACCOUNTS_PREFS = "moc_mien_local_accounts";
    private static final String SESSION_PREFS = "moc_mien_session";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 120_000;

    private LocalAuthRepository() { }

    public static boolean register(Context context, String name, String identifier, String password) {
        String key = accountKey(identifier);
        SharedPreferences accounts = context.getSharedPreferences(ACCOUNTS_PREFS, Context.MODE_PRIVATE);
        if (accounts.contains(key + ".hash")) return false;
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt);
        boolean stored = accounts.edit()
                .putString(key + ".salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(key + ".hash", Base64.encodeToString(hash, Base64.NO_WRAP))
                .putString(key + ".name", name.trim())
                .putString("last_identifier", AuthValidation.normalizeIdentifier(identifier))
                .commit();
        if (stored) createSession(context, name.trim(), identifier.trim());
        Arrays.fill(hash, (byte) 0);
        return stored;
    }

    public static boolean authenticate(Context context, String identifier, String password) {
        String key = accountKey(identifier);
        SharedPreferences accounts = context.getSharedPreferences(ACCOUNTS_PREFS, Context.MODE_PRIVATE);
        String encodedSalt = accounts.getString(key + ".salt", null);
        String encodedHash = accounts.getString(key + ".hash", null);
        if (encodedSalt == null || encodedHash == null) return false;
        byte[] salt = Base64.decode(encodedSalt, Base64.NO_WRAP);
        byte[] expected = Base64.decode(encodedHash, Base64.NO_WRAP);
        byte[] actual = derive(password.toCharArray(), salt);
        boolean matches = constantTimeEquals(expected, actual);
        Arrays.fill(actual, (byte) 0);
        if (matches) {
            String name = accounts.getString(key + ".name", identifier.trim());
            createSession(context, name, identifier.trim());
        }
        return matches;
    }

    private static void createSession(Context context, String name, String identifier) {
        context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean("logged_in", true)
                .putString("name", name)
                .putString("identifier", identifier)
                .apply();
    }

    public static boolean hasActiveSession(Context context) {
        return context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
                .getBoolean("logged_in", false);
    }

    public static String getSessionName(Context context) {
        return context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
                .getString("name", "");
    }

    public static String getSessionIdentifier(Context context) {
        return context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
                .getString("identifier", "");
    }

    public static String getLastRegisteredIdentifier(Context context) {
        return context.getSharedPreferences(ACCOUNTS_PREFS, Context.MODE_PRIVATE)
                .getString("last_identifier", "");
    }

    public static String getRegisteredName(Context context, String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return "";
        String key = accountKey(identifier);
        return context.getSharedPreferences(ACCOUNTS_PREFS, Context.MODE_PRIVATE)
                .getString(key + ".name", "");
    }

    public static void logout(Context context) {
        context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private static String accountKey(String identifier) {
        return "account." + AuthValidation.normalizeIdentifier(identifier);
    }

    private static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    private static boolean constantTimeEquals(byte[] first, byte[] second) {
        if (first.length != second.length) return false;
        int difference = 0;
        for (int i = 0; i < first.length; i++) difference |= first[i] ^ second[i];
        return difference == 0;
    }
}
