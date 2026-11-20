package com.coffeeshop.client.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coffeeshop.client.data.OrderRepository;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.CreateOrderRequestDto;
import com.coffeeshop.client.data.remote.dto.OrderResponseDto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OrderViewModel extends AndroidViewModel {
    private final OrderRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final MutableLiveData<ApiResult<OrderResponseDto>> createOrder = new MutableLiveData<>();
    private final MutableLiveData<ApiResult<List<OrderResponseDto>>> orders = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<ApiResult<OrderResponseDto>> getCreateOrderResult() { return createOrder; }
    public LiveData<ApiResult<List<OrderResponseDto>>> getOrdersResult() { return orders; }

    public void createOrder(List<CreateOrderRequestDto.Item> items) {
        createOrder.setValue(ApiResult.loading());
        CreateOrderRequestDto request = new CreateOrderRequestDto(items);
        executor.execute(() -> createOrder.postValue(repository.createRemoteOrder(request)));
    }

    public void loadOrders() {
        orders.setValue(ApiResult.loading());
        executor.execute(() -> orders.postValue(repository.fetchRemoteOrders()));
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
