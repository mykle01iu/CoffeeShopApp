package com.example.myapplication.data;

import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Category and product requests to the CoffeeShop backend. */
public final class BackendApi {
    // Android emulator -> backend on the development host.
    // For a physical device, use the host computer's LAN IP instead.
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static final int TIMEOUT_MS = 12000;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final SessionManager session;

    public BackendApi(SessionManager session) {
        this.session = session;
    }

    public static final class ApiError {
        public final int statusCode;
        public final String message;

        private ApiError(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(ApiError error);
    }

    private interface Parser<T> {
        T parse(String body) throws Exception;
    }

    public void getCategories(Callback<List<Category>> callback) {
        request("GET", "categories", null, body -> {
            JSONArray array = new JSONArray(body);
            List<Category> categories = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                categories.add(new Category((int) item.optLong("id"), item.optString("code"),
                        item.optString("name"), nullableString(item, "description"),
                        item.optBoolean("available", true)));
            }
            return categories;
        }, callback);
    }

    public void getProducts(Callback<List<Product>> callback) {
        request("GET", "products", null, body -> {
            JSONArray array = new JSONArray(body);
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                products.add(new Product((int) item.optLong("id"), item.optString("category", ""),
                        item.optString("name"), item.optDouble("price"),
                        nullableString(item, "description"), nullableString(item, "imageUrl"),
                        item.optBoolean("available", true)));
            }
            return products;
        }, callback);
    }

    public void createCategory(String code, String name, String description, Callback<Category> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("code", code);
            body.put("name", name);
            body.put("description", description);
            body.put("available", true);
        } catch (Exception exception) {
            callback.onError(new ApiError(0, exception.getMessage()));
            return;
        }
        request("POST", "categories", body, BackendApi::parseCategory, callback);
    }

    public void updateCategory(Category category, String code, String name, String description,
                               Callback<Category> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("code", code);
            body.put("name", name);
            body.put("description", description);
            body.put("available", category.available);
        } catch (Exception exception) {
            callback.onError(new ApiError(0, exception.getMessage()));
            return;
        }
        request("PUT", "categories/" + category.id, body, BackendApi::parseCategory, callback);
    }

    public void deleteCategory(int id, Callback<Void> callback) {
        request("DELETE", "categories/" + id, null, body -> null, callback);
    }

    public void createProduct(String categoryCode, String name, double price, String description,
                              String imageUrl, Callback<Product> callback) {
        JSONObject body = productBody(categoryCode, name, price, description, imageUrl, true);
        if (body == null) {
            callback.onError(new ApiError(0, "Không thể tạo dữ liệu sản phẩm."));
            return;
        }
        request("POST", "products", body, BackendApi::parseProduct, callback);
    }

    public void updateProduct(Product product, String categoryCode, String name, double price,
                              String description, String imageUrl, Callback<Product> callback) {
        JSONObject body = productBody(categoryCode, name, price, description, imageUrl,
                product.available);
        if (body == null) {
            callback.onError(new ApiError(0, "Không thể tạo dữ liệu sản phẩm."));
            return;
        }
        request("PUT", "products/" + product.id, body, BackendApi::parseProduct, callback);
    }

    public void deleteProduct(int id, Callback<Void> callback) {
        request("DELETE", "products/" + id, null, body -> null, callback);
    }

    private static JSONObject productBody(String categoryCode, String name, double price,
                                          String description, String imageUrl, boolean available) {
        try {
            JSONObject body = new JSONObject();
            body.put("category", categoryCode);
            body.put("name", name);
            body.put("price", price);
            body.put("description", description);
            body.put("imageUrl", imageUrl == null || imageUrl.trim().isEmpty()
                    ? JSONObject.NULL : imageUrl.trim());
            body.put("available", available);
            return body;
        } catch (Exception ignored) {
            return null;
        }
    }

    private <T> void request(String method, String path, JSONObject requestBody,
                             Parser<T> parser, Callback<T> callback) {
        final String token = session.getToken();
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
                connection.setRequestMethod(method);
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("Accept", "application/json");
                if (token != null && !token.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
                if (requestBody != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String responseBody = readBody(stream);
                if (status < 200 || status >= 300) {
                    throw new ApiRequestException(status, errorMessage(responseBody, status));
                }
                T result = parser.parse(responseBody);
                MAIN_HANDLER.post(() -> callback.onSuccess(result));
            } catch (ApiRequestException exception) {
                ApiError error = new ApiError(exception.statusCode, exception.getMessage());
                MAIN_HANDLER.post(() -> callback.onError(error));
            } catch (Exception exception) {
                String message = exception.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Không kết nối được backend. Kiểm tra backend và địa chỉ API.";
                }
                ApiError error = new ApiError(0, message);
                MAIN_HANDLER.post(() -> callback.onError(error));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static Category parseCategory(String body) throws Exception {
        JSONObject item = new JSONObject(body);
        return new Category((int) item.optLong("id"), item.optString("code"),
                item.optString("name"), nullableString(item, "description"),
                item.optBoolean("available", true));
    }

    private static Product parseProduct(String body) throws Exception {
        JSONObject item = new JSONObject(body);
        return new Product((int) item.optLong("id"), item.optString("category", ""),
                item.optString("name"), item.optDouble("price"),
                nullableString(item, "description"), nullableString(item, "imageUrl"),
                item.optBoolean("available", true));
    }

    private static String nullableString(JSONObject item, String key) {
        return item.isNull(key) ? "" : item.optString(key, "");
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
            JSONObject error = new JSONObject(body);
            String message = error.optString("message", "");
            if (!message.isEmpty()) return message;
            JSONObject nested = error.optJSONObject("error");
            if (nested != null) {
                message = nested.optString("message", "");
                if (!message.isEmpty()) return message;
            }
        } catch (Exception ignored) {
            // Use the HTTP status if the backend did not return a JSON error.
        }
        if (status == 401) return "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
        if (status == 403) return "Tài khoản không có quyền ADMIN hoặc phiên đăng nhập không còn hợp lệ.";
        if (status == 404) return "Dữ liệu không còn tồn tại trên backend.";
        if (status == 409) return "Mã danh mục đã tồn tại.";
        return "Backend trả về HTTP " + status + ".";
    }

    private static final class ApiRequestException extends Exception {
        final int statusCode;

        ApiRequestException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}
