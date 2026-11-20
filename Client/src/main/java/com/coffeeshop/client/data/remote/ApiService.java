package com.coffeeshop.client.data.remote;

import com.coffeeshop.client.data.remote.dto.AuthResponseDto;
import com.coffeeshop.client.data.remote.dto.CategoryDto;
import com.coffeeshop.client.data.remote.dto.CreateOrderRequestDto;
import com.coffeeshop.client.data.remote.dto.LoginRequestDto;
import com.coffeeshop.client.data.remote.dto.OrderResponseDto;
import com.coffeeshop.client.data.remote.dto.ProductDto;
import com.coffeeshop.client.data.remote.dto.RegisterRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @POST("auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDto request);

    @GET("categories")
    Call<List<CategoryDto>> getCategories();

    @GET("products")
    Call<List<ProductDto>> getProducts(@Query("category") String category);

    @POST("orders")
    Call<OrderResponseDto> createOrder(@Body CreateOrderRequestDto request);

    @GET("orders")
    Call<List<OrderResponseDto>> getOrders();
}
