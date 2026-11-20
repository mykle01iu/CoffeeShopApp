package com.coffeeshop.client.data.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentRepository {
    private static PaymentRepository instance;
    private final List<PaymentMethod> methodsList = new ArrayList<>();
    private final MutableLiveData<List<PaymentMethod>> methodsLiveData = new MutableLiveData<>(new ArrayList<>());

    private PaymentRepository() {
        // Initial default method is COD
        methodsList.add(new PaymentMethod(
                "cod",
                PaymentType.COD,
                "Tiền mặt khi nhận hàng",
                "Thanh toán trực tiếp khi nhận hàng (COD)",
                true
        ));
        methodsLiveData.setValue(new ArrayList<>(methodsList));
    }

    public static synchronized PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }

    public LiveData<List<PaymentMethod>> getMethodsLiveData() {
        return methodsLiveData;
    }

    public List<PaymentMethod> getMethods() {
        return new ArrayList<>(methodsList);
    }

    public PaymentMethod getDefaultMethod() {
        for (PaymentMethod m : methodsList) {
            if (m.isDefault()) return m;
        }
        return methodsList.isEmpty() ? null : methodsList.get(0);
    }

    public synchronized void setDefault(String id) {
        if (id == null) return;
        List<PaymentMethod> updated = new ArrayList<>();
        for (PaymentMethod m : methodsList) {
            boolean isDef = m.getId().equals(id);
            updated.add(new PaymentMethod(m.getId(), m.getType(), m.getDisplayName(), m.getDetail(), isDef));
        }
        methodsList.clear();
        methodsList.addAll(updated);
        methodsLiveData.setValue(new ArrayList<>(methodsList));
    }

    public synchronized void addWallet(String provider, String phone) {
        String maskedPhone = maskPhone(phone);
        String displayName = "Ví " + provider;
        String detail = "Số điện thoại " + maskedPhone;
        String id = "wallet_" + UUID.randomUUID().toString();

        boolean setAsDefault = false;
        PaymentMethod method = new PaymentMethod(id, PaymentType.WALLET, displayName, detail, setAsDefault);
        methodsList.add(method);
        methodsLiveData.setValue(new ArrayList<>(methodsList));
    }

    public synchronized void addCard(String brand, String last4Digits, String expiry) {
        String displayName = brand + " Card";
        String detail = brand + " •••• " + last4Digits + " · hết hạn " + expiry;
        String id = "card_" + UUID.randomUUID().toString();

        boolean setAsDefault = false;
        PaymentMethod method = new PaymentMethod(id, PaymentType.CARD, displayName, detail, setAsDefault);
        methodsList.add(method);
        methodsLiveData.setValue(new ArrayList<>(methodsList));
    }

    public synchronized void remove(String id) {
        if (id == null || id.equals("cod")) return; // COD cannot be deleted
        PaymentMethod toRemove = null;
        for (PaymentMethod m : methodsList) {
            if (m.getId().equals(id)) {
                toRemove = m;
                break;
            }
        }
        if (toRemove != null) {
            boolean wasDefault = toRemove.isDefault();
            methodsList.remove(toRemove);
            if (wasDefault) {
                // If deleted method was default, make COD default
                setDefault("cod");
            } else {
                methodsLiveData.setValue(new ArrayList<>(methodsList));
            }
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone != null ? phone : "";
        if (phone.length() == 10) {
            return phone.substring(0, 4) + "***" + phone.substring(7);
        }
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 3);
    }
}
