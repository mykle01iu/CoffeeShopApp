package com.example.myapplication.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Authentication calls to the CoffeeShop backend. */
public final class AuthApi {
    private static final String TAG = "AuthApi";
    // Android emulator -> backend running on the development host.
    // For a physical device, replace the host with the computer's LAN IP.
    private static final String LOGIN_URL = "http://10.0.2.2:8080/api/auth/login";
    private static final int TIMEOUT_MS = 10000;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static final class LoginResult {
        public final String token;
        public final String name;
        public final String email;
        public final String role;

        private LoginResult(String token, String name, String email, String role) {
            this.token = token;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    public interface Callback {
        void onSuccess(LoginResult result);
        void onError(String message);
    }

    public void login(String email, String password, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject request = new JSONObject();
                request.put("email", email);
                request.put("password", password);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(request.toString().getBytes(StandardCharsets.UTF_8));
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String body = readBody(stream);
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException(errorMessage(body, status));
                }

                JSONObject response = new JSONObject(body);
                String token = response.optString("token", "");
                String responseEmail = response.optString("email", email);
                String name = response.optString("name", "");
                String role = response.optString("role", "");
                if (token.isEmpty() || role.isEmpty()) {
                    throw new IllegalStateException("Backend phản hồi thiếu JWT hoặc role.");
                }

                LoginResult result = new LoginResult(token, name, responseEmail, role);
                MAIN_HANDLER.post(() -> callback.onSuccess(result));
            } catch (Exception exception) {
                Log.e(TAG, "Login request failed to " + LOGIN_URL, exception);
                String message = exception.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Không kết nối được backend. Kiểm tra backend và địa chỉ API.";
                }
                String error = message;
                MAIN_HANDLER.post(() -> callback.onError(error));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static String readBody(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            return body.toString();
        }
    }

    private static String errorMessage(String body, int status) {
        try {
            String message = new JSONObject(body).optString("message", "");
            if (!message.isEmpty()) return message;
        } catch (Exception ignored) {
            // Fall through to the HTTP status if the error body is not JSON.
        }
        if (status == 401 || status == 400) return "Email hoặc mật khẩu không chính xác.";
        return "Backend trả về HTTP " + status + ".";
    }
}
