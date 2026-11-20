package com.coffeeshop.client.data.remote;

import com.coffeeshop.client.data.remote.dto.ApiErrorDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class ApiExecutor {
    private static final Gson GSON = new Gson();

    private ApiExecutor() { }

    public static <T> ApiResult<T> safeApiCall(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                if (response.body() == null) return ApiResult.error("Máy chủ không trả dữ liệu.");
                return ApiResult.success(response.body());
            }
            return ApiResult.error(readError(response.code(), response.errorBody()));
        } catch (IOException exception) {
            return ApiResult.error("Không kết nối được máy chủ");
        } catch (Exception exception) {
            String message = exception.getMessage();
            return ApiResult.error(message == null || message.trim().isEmpty()
                    ? "Đã xảy ra lỗi. Vui lòng thử lại." : message);
        }
    }

    private static String readError(int code, ResponseBody body) {
        if (body != null) {
            try {
                String json = body.string();
                if (json != null && !json.trim().isEmpty()) {
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        String msg = obj.get("message").getAsString();
                        if (msg != null && !msg.trim().isEmpty()) {
                            return msg;
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
        return "Yêu cầu không hợp lệ (" + code + ").";
    }
}
