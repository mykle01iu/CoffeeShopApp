package com.coffeeshop.client.data;

import com.coffeeshop.client.model.PointTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PointsRepository {
    private PointsRepository() { }

    private static final List<PointTransaction> TRANSACTIONS = new ArrayList<>(Arrays.asList(
            new PointTransaction(1, "Đơn hàng #082121", 20, "04/11/2026"),
            new PointTransaction(2, "Đổi Voucher Giảm 20k", -50, "02/11/2026"),
            new PointTransaction(3, "Thưởng đăng ký tài khoản", 50, "01/11/2026"),
            new PointTransaction(4, "Thành viên Mộc Miên mới", 100, "01/11/2026")
    ));

    private static int nextId = 5;

    public static synchronized List<PointTransaction> getTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(TRANSACTIONS));
    }

    public static synchronized void addTransaction(String title, int points, String date) {
        PointTransaction t = new PointTransaction(nextId++, title, points, date);
        TRANSACTIONS.add(0, t);
    }

    public static synchronized int getTotalPoints() {
        int sum = 0;
        for (PointTransaction t : TRANSACTIONS) {
            sum += t.points;
        }
        return sum;
    }

    public static String getTier() {
        return getTotalPoints() >= 200 ? "Vàng" : "Thân Thiết";
    }

    public static int getRemainingToNextTier() {
        return Math.max(0, 200 - getTotalPoints());
    }

    public static synchronized List<PointTransaction> getFilteredTransactions(int filterType) {
        List<PointTransaction> all = getTransactions();
        if (filterType == 1) {
            List<PointTransaction> result = new ArrayList<>();
            for (PointTransaction t : all) {
                if (t.points > 0) result.add(t);
            }
            return Collections.unmodifiableList(result);
        } else if (filterType == 2) {
            List<PointTransaction> result = new ArrayList<>();
            for (PointTransaction t : all) {
                if (t.points < 0) result.add(t);
            }
            return Collections.unmodifiableList(result);
        }
        return all;
    }
}
