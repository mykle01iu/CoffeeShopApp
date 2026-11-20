package com.coffeeshop.client.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coffeeshop.client.data.MenuRepository;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.CategoryDto;
import com.coffeeshop.client.data.remote.dto.ProductDto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MenuViewModel extends AndroidViewModel {
    private final MenuRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final MutableLiveData<ApiResult<List<CategoryDto>>> categories = new MutableLiveData<>();
    private final MutableLiveData<ApiResult<List<ProductDto>>> products = new MutableLiveData<>();

    public MenuViewModel(@NonNull Application application) {
        super(application);
        repository = new MenuRepository(application);
    }

    public LiveData<ApiResult<List<CategoryDto>>> getCategories() { return categories; }
    public LiveData<ApiResult<List<ProductDto>>> getProducts() { return products; }

    public void loadCategories() {
        categories.setValue(ApiResult.loading());
        executor.execute(() -> categories.postValue(repository.getCategories()));
    }

    public void loadProducts(String categoryCode) {
        products.setValue(ApiResult.loading());
        executor.execute(() -> products.postValue(repository.getProducts(categoryCode)));
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
