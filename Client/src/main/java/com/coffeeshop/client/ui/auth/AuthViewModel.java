package com.coffeeshop.client.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coffeeshop.client.data.AuthRepository;
import com.coffeeshop.client.data.remote.ApiResult;
import com.coffeeshop.client.data.remote.dto.AuthResponseDto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<ApiResult<AuthResponseDto>> authState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<ApiResult<AuthResponseDto>> getAuthState() {
        return authState;
    }

    public void login(String identifier, String password) {
        authState.setValue(ApiResult.loading());
        executor.execute(() -> authState.postValue(repository.login(identifier, password)));
    }

    public void register(String name, String email, String password) {
        authState.setValue(ApiResult.loading());
        executor.execute(() -> authState.postValue(repository.register(name, email, password)));
    }

    public void register(String name, String identifier, String password, boolean phone) {
        authState.setValue(ApiResult.loading());
        executor.execute(() -> authState.postValue(repository.register(name, identifier, password, phone)));
    }

    public void reset() {
        authState.setValue(null);
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
