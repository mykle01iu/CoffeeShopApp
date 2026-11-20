package com.coffeeshop.client.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.coffeeshop.client.data.local.TokenManager;
import com.coffeeshop.client.ui.auth.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String urlPath = originalRequest.url().encodedPath();

        // Skip adding token for login and register requests
        if (urlPath.contains("auth/login") || urlPath.contains("auth/register")) {
            return chain.proceed(originalRequest);
        }

        String token = TokenManager.getToken(appContext);
        Request.Builder builder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(builder.build());

        // Handle 401 Unauthorized for authenticated endpoints
        if (response.code() == 401) {
            TokenManager.clear(appContext);
            new Handler(Looper.getMainLooper()).post(() -> {
                Intent intent = new Intent(appContext, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            });
        }

        return response;
    }
}
