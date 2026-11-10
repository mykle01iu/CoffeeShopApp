package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.coffeeshop.client.R;
import com.coffeeshop.client.model.Voucher;
import com.coffeeshop.client.model.VoucherStatus;
import com.coffeeshop.client.model.VoucherType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class VoucherRepository {
    private static final String PREF_NAME = "moc_mien_vouchers_store";
    private static final String KEY_SELECTED = "selected_voucher_code";

    private VoucherRepository() { }

    private static final List<Voucher> MOCK_VOUCHERS = new ArrayList<>();

    static {
        MOCK_VOUCHERS.add(new Voucher("GIAM20", "Giảm 20% đơn đầu tiên", "Áp dụng cho đơn hàng đầu tiên", "HSD 30/11/2026", R.drawable.ic_favorite_border, VoucherStatus.ACTIVE, VoucherType.PERCENT, 20, 0, 30000, true, null));
        MOCK_VOUCHERS.add(new Voucher("FREESHIP", "Miễn phí giao hàng", "Cho đơn từ 50.000đ", "HSD 15/12/2026", R.drawable.ic_nav_cup, VoucherStatus.ACTIVE, VoucherType.FREESHIP, 0, 50000, null, false, null));
        MOCK_VOUCHERS.add(new Voucher("VOUCHER20K", "Giảm 20.000đ", "Đổi từ 50 điểm tích lũy", "Đã dùng 03/11/2026", R.drawable.ic_orders, VoucherStatus.USED, VoucherType.FIXED, 20000, 0, null, false, "03/11/2026"));
        MOCK_VOUCHERS.add(new Voucher("WELCOME7", "Giảm 7.000đ", "Quà chào mừng thành viên", "Hết hạn 31/10/2026", R.drawable.ic_splash_cup, VoucherStatus.EXPIRED, VoucherType.FIXED, 7000, 0, null, false, null));
    }

    public static List<Voucher> getAllVouchers() {
        return Collections.unmodifiableList(MOCK_VOUCHERS);
    }

    public static List<Voucher> getVouchersByStatus(VoucherStatus status) {
        List<Voucher> result = new ArrayList<>();
        for (Voucher v : MOCK_VOUCHERS) {
            if (v.status == status) {
                result.add(v);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static int getVoucherCount(VoucherStatus status) {
        int count = 0;
        for (Voucher v : MOCK_VOUCHERS) {
            if (v.status == status) count++;
        }
        return count;
    }

    public static int getActiveCount() {
        return getVoucherCount(VoucherStatus.ACTIVE);
    }

    public static Voucher getVoucherByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        for (Voucher v : MOCK_VOUCHERS) {
            if (v.code.equalsIgnoreCase(code.trim())) {
                return v;
            }
        }
        return null;
    }

    public static boolean isFirstOrder(Context context) {
        return OrderRepository.getOrders(context).isEmpty();
    }

    public static String validate(Context context, String code, int subtotal) {
        Voucher v = getVoucherByCode(code);
        if (v == null) return "Mã không tồn tại";
        if (v.status == VoucherStatus.USED) return "Mã đã dùng";
        if (v.status == VoucherStatus.EXPIRED) return "Mã đã hết hạn";
        if (v.firstOrderOnly && !isFirstOrder(context)) return "Mã chỉ dùng cho đơn đầu tiên";
        if (subtotal < v.minOrder) {
            String minStr = NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(v.minOrder) + "đ";
            return "Cần đơn từ " + minStr + " để dùng mã này";
        }
        return null; // Valid!
    }

    public static int calculateDiscount(Voucher voucher, int subtotal, int shippingFee) {
        if (voucher == null) return 0;
        if (voucher.type == VoucherType.PERCENT) {
            int discount = (int) (subtotal * (voucher.value / 100.0f));
            if (voucher.maxDiscount != null && discount > voucher.maxDiscount) {
                discount = voucher.maxDiscount;
            }
            return Math.min(discount, subtotal);
        } else if (voucher.type == VoucherType.FIXED) {
            return Math.min(voucher.value, subtotal);
        } else if (voucher.type == VoucherType.FREESHIP) {
            return shippingFee;
        }
        return 0;
    }

    public static void markUsed(Context context, String code, String date) {
        if (code == null) return;
        for (int i = 0; i < MOCK_VOUCHERS.size(); i++) {
            Voucher v = MOCK_VOUCHERS.get(i);
            if (v.code.equalsIgnoreCase(code.trim())) {
                String note = "Đã dùng " + (date != null ? date : "");
                Voucher updated = new Voucher(v.code, v.title, v.condition, note, v.iconRes, VoucherStatus.USED, v.type, v.value, v.minOrder, v.maxDiscount, v.firstOrderOnly, date);
                MOCK_VOUCHERS.set(i, updated);
                break;
            }
        }
        clearSelectedVoucherCode(context);
    }

    public static void restore(Context context, String code) {
        if (code == null) return;
        for (int i = 0; i < MOCK_VOUCHERS.size(); i++) {
            Voucher v = MOCK_VOUCHERS.get(i);
            if (v.code.equalsIgnoreCase(code.trim())) {
                String note = "HSD 30/11/2026";
                Voucher updated = new Voucher(v.code, v.title, v.condition, note, v.iconRes, VoucherStatus.ACTIVE, v.type, v.value, v.minOrder, v.maxDiscount, v.firstOrderOnly, null);
                MOCK_VOUCHERS.set(i, updated);
                break;
            }
        }
    }

    public static String getSelectedVoucherCode(Context context) {
        if (context == null) return null;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_SELECTED, null);
    }

    public static void setSelectedVoucherCode(Context context, String code) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SELECTED, code).apply();
    }

    public static void clearSelectedVoucherCode(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().remove(KEY_SELECTED).apply();
    }
}
