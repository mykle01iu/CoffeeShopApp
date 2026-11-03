package com.coffeeshop.client.ui.auth;

import android.util.Patterns;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AuthValidation {
    private static final Pattern VN_PHONE = Pattern.compile("^(?:0|\\+?84)(?:3|5|7|8|9)[0-9]{8}$");

    private AuthValidation() { }

    public static boolean isValidIdentifier(String value) {
        String input = value == null ? "" : value.trim();
        return Patterns.EMAIL_ADDRESS.matcher(input).matches() || VN_PHONE.matcher(input).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null && Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches();
    }

    public static boolean isStrongEnoughPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isLetter(ch)) hasLetter = true;
            if (Character.isDigit(ch)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    public static String normalizeIdentifier(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("@")) return normalized;
        normalized = normalized.replace(" ", "").replace("-", "");
        if (normalized.startsWith("+84")) return "0" + normalized.substring(3);
        if (normalized.startsWith("84") && normalized.length() == 11) return "0" + normalized.substring(2);
        return normalized;
    }
}
