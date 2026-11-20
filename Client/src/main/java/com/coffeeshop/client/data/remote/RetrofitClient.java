package com.coffeeshop.client.data.remote;

import android.content.Context;

import com.coffeeshop.client.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Retrofit dùng chung cho các API của ứng dụng. */
public final class RetrofitClient {
    private static volatile Retrofit instance;

    private RetrofitClient() { }

    public static Retrofit getInstance(Context context) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.redactHeader("Authorization");
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                            .addInterceptor(logging)
                            .build();

                    Gson gson = new GsonBuilder().create();
                    instance = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return instance;
    }

    public static ApiService getApiService(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}
