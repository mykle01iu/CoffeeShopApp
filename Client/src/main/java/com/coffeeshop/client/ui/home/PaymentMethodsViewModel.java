package com.coffeeshop.client.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.coffeeshop.client.data.payment.PaymentMethod;
import com.coffeeshop.client.data.payment.PaymentRepository;

import java.util.List;

public class PaymentMethodsViewModel extends ViewModel {
    private final LiveData<List<PaymentMethod>> methods = PaymentRepository.getInstance().getMethodsLiveData();

    public LiveData<List<PaymentMethod>> getMethods() {
        return methods;
    }

    public void setDefault(String id) {
        PaymentRepository.getInstance().setDefault(id);
    }

    public void remove(String id) {
        PaymentRepository.getInstance().remove(id);
    }

    public void addWallet(String provider, String phone) {
        PaymentRepository.getInstance().addWallet(provider, phone);
    }

    public void addCard(String cardNumber, String cardHolder, String expiryDate) {
        PaymentRepository.getInstance().addCard(cardNumber, cardHolder, expiryDate);
    }
}
