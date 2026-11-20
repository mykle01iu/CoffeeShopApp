package com.coffeeshop.client.data.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PaymentRepository {
    private static PaymentRepository instance;
    private final List<PaymentMethod> methodList = new ArrayList<>();
    private final MutableLiveData<List<PaymentMethod>> methodsLiveData = new MutableLiveData<>(Collections.emptyList());
    private int counter = 1;

    private PaymentRepository() {
        methodList.add(PaymentMethod.createCod());
        notifyChanges();
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

    public synchronized List<PaymentMethod> getMethods() {
        return new ArrayList<>(methodList);
    }

    public synchronized PaymentMethod getDefaultMethod() {
        for (PaymentMethod m : methodList) {
            if (m.isDefault()) return m;
        }
        return methodList.isEmpty() ? PaymentMethod.createCod() : methodList.get(0);
    }

    public synchronized void setDefault(String id) {
        if (id == null) return;
        List<PaymentMethod> updated = new ArrayList<>();
        for (PaymentMethod m : methodList) {
            boolean isDef = m.getId().equals(id);
            updated.add(new PaymentMethod(
                    m.getId(), m.getType(), m.getDisplayName(), m.getDetail(),
                    isDef, m.getProvider(), m.getMaskedPhone(), m.getCardBrand(),
                    m.getCardNumberLast4(), m.getExpiryDate()
            ));
        }
        methodList.clear();
        methodList.addAll(updated);
        notifyChanges();
    }

    public synchronized void remove(String id) {
        if (id == null) return;
        PaymentMethod target = null;
        for (PaymentMethod m : methodList) {
            if (m.getId().equals(id)) {
                target = m;
                break;
            }
        }

        if (target == null || target.getType() == PaymentType.COD) {
            return; // COD cannot be deleted
        }

        boolean wasDefault = target.isDefault();
        methodList.remove(target);

        if (wasDefault) {
            // Set COD as default
            setDefault("cod_default");
        } else {
            notifyChanges();
        }
    }

    public synchronized PaymentMethod addWallet(String provider, String rawPhone) {
        String masked = maskPhone(rawPhone);
        String displayName = "Ví " + (provider != null ? provider : "điện tử");
        String detail = masked;
        String id = "wallet_" + (counter++);
        boolean setAsDefault = methodList.size() == 1 && methodList.get(0).getType() == PaymentType.COD;

        PaymentMethod wallet = new PaymentMethod(
                id, PaymentType.WALLET, displayName, detail, setAsDefault,
                provider, masked, null, null, null
        );

        methodList.add(wallet);
        if (setAsDefault) {
            setDefault(id);
        } else {
            notifyChanges();
        }
        return wallet;
    }

    public synchronized PaymentMethod addCard(String rawCardNumber, String cardHolder, String expiryDate) {
        String cleanNumber = rawCardNumber != null ? rawCardNumber.replaceAll("\\s+", "") : "";
        String brand = detectCardBrand(cleanNumber);
        String last4 = cleanNumber.length() >= 4 ? cleanNumber.substring(cleanNumber.length() - 4) : "0000";
        String displayName = "Thẻ " + brand;
        String detail = brand + " •••• " + last4 + " · HSD " + (expiryDate != null ? expiryDate : "");
        String id = "card_" + (counter++);
        boolean setAsDefault = methodList.size() == 1 && methodList.get(0).getType() == PaymentType.COD;

        PaymentMethod card = new PaymentMethod(
                id, PaymentType.CARD, displayName, detail, setAsDefault,
                null, null, brand, last4, expiryDate
        );

        methodList.add(card);
        if (setAsDefault) {
            setDefault(id);
        } else {
            notifyChanges();
        }
        return card;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone != null ? phone : "";
        String clean = phone.trim();
        if (clean.length() == 10) {
            return clean.substring(0, 4) + "***" + clean.substring(7);
        }
        return clean.substring(0, 3) + "***" + clean.substring(clean.length() - 3);
    }

    public static String detectCardBrand(String number) {
        if (number == null) return "Thẻ ngân hàng";
        String clean = number.replaceAll("\\s+", "");
        if (clean.startsWith("4")) return "Visa";
        if (clean.matches("^(5[1-5]|2[2-7]).*")) return "Mastercard";
        if (clean.startsWith("35")) return "JCB";
        if (clean.startsWith("9704")) return "Napass";
        return "Thẻ ngân hàng";
    }

    public static boolean isLuhnValid(String number) {
        if (number == null) return false;
        String clean = number.replaceAll("\\s+", "");
        if (clean.length() < 13 || clean.length() > 19 || !clean.matches("\\d+")) return false;
        int sum = 0;
        boolean alternate = false;
        for (int i = clean.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(clean.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private void notifyChanges() {
        methodsLiveData.setValue(new ArrayList<>(methodList));
    }
}
