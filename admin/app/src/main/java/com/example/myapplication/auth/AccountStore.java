package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountStore {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_STAFF = "staff";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_PENDING = "pending";

    // Giữ nguyên tên file prefs để tài khoản đã đăng ký trước đó vẫn còn
    private static final String PREFS_NAME = "registered_accounts";

    public enum LoginResult { SUCCESS, WRONG_CREDENTIALS, PENDING_APPROVAL, ERROR }

    public enum RegisterResult { ADMIN_CREATED, PENDING_APPROVAL, ALREADY_EXISTS, SAVE_FAILED }

    private final SharedPreferences prefs;
    private final SessionManager session;

    public AccountStore(Context context, SessionManager session) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.session = session;
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public LoginResult login(String email, String password) {
        String normalized = normalize(email);
        String storedHash = prefs.getString("hash:" + normalized, null);
        if (storedHash == null) {
            return LoginResult.WRONG_CREDENTIALS;
        }

        try {
            byte[] salt = Base64.decode(prefs.getString("salt:" + normalized, ""), Base64.NO_WRAP);
            byte[] expected = Base64.decode(storedHash, Base64.NO_WRAP);
            byte[] actual = PasswordHasher.hash(password, salt);
            if (!MessageDigest.isEqual(expected, actual)) {
                return LoginResult.WRONG_CREDENTIALS;
            }
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            return LoginResult.ERROR;
        }

        if (!STATUS_APPROVED.equals(prefs.getString("status:" + normalized, STATUS_PENDING))) {
            return LoginResult.PENDING_APPROVAL;
        }

        // Đăng nhập thành công: lưu session kèm role
        session.createSession(normalized, roleOf(normalized));
        return LoginResult.SUCCESS;
    }

    public RegisterResult register(String name, String phone, String email, String password) {
        String normalized = normalize(email);
        if (prefs.contains("hash:" + normalized)) {
            return RegisterResult.ALREADY_EXISTS;
        }

        // Tài khoản đầu tiên trên thiết bị là admin và được duyệt tự động
        boolean isFirst = !hasAnyAccount();
        String status = isFirst ? STATUS_APPROVED : STATUS_PENDING;
        String role = isFirst ? ROLE_ADMIN : ROLE_STAFF;

        byte[] salt = PasswordHasher.newSalt();
        try {
            String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
            String encodedHash = Base64.encodeToString(PasswordHasher.hash(password, salt), Base64.NO_WRAP);

            boolean saved = prefs.edit()
                    .putString("salt:" + normalized, encodedSalt)
                    .putString("hash:" + normalized, encodedHash)
                    .putString("status:" + normalized, status)
                    .putString("role:" + normalized, role)
                    .putString("name:" + normalized, name)
                    .putString("phone:" + normalized, phone)
                    .commit();

            if (!saved) {
                return RegisterResult.SAVE_FAILED;
            }
        } catch (GeneralSecurityException e) {
            return RegisterResult.SAVE_FAILED;
        }

        return isFirst ? RegisterResult.ADMIN_CREATED : RegisterResult.PENDING_APPROVAL;
    }

    public String roleOf(String normalizedEmail) {
        return prefs.getString("role:" + normalizedEmail, ROLE_STAFF);
    }

    // Quyền admin lấy từ session hiện tại, không còn lưu trong biến của class
    public boolean isAdmin() {
        return session.isAdmin();
    }

    public List<String> pendingEmails() {
        ArrayList<String> result = new ArrayList<>();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("status:") && STATUS_PENDING.equals(prefs.getString(key, ""))) {
                result.add(key.substring("status:".length()));
            }
        }
        return result;
    }

    public String nameOf(String email) {
        return prefs.getString("name:" + email, "Tài khoản mới");
    }

    public void approve(String email) {
        if (!isAdmin()) return;
        prefs.edit().putString("status:" + email, STATUS_APPROVED).apply();
    }

    private boolean hasAnyAccount() {
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("hash:")) return true;
        }
        return false;
    }
}