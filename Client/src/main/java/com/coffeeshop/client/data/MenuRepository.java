package com.coffeeshop.client.data;

import android.content.Context;

import com.coffeeshop.client.data.remote.ApiExecutor;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.ApiService;
import com.coffeeshop.client.data.remote.RetrofitClient;
import com.coffeeshop.client.data.remote.dto.CategoryDto;
import com.coffeeshop.client.data.remote.dto.ProductDto;

import java.util.List;

public final class MenuRepository {
    private final ApiService api;

    public MenuRepository(Context context) {
        api = RetrofitClient.getApiService(context);
    }

    public ApiResult<List<CategoryDto>> getCategories() {
        return ApiExecutor.safeApiCall(api.getCategories());
    }

    public ApiResult<List<ProductDto>> getProducts(String categoryCode) {
        return ApiExecutor.safeApiCall(api.getProducts(categoryCode));
    }
}
