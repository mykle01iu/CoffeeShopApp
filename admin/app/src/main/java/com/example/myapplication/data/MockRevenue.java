package com.example.myapplication.data;

import java.util.Calendar;

/** Deterministic sample revenue used until the backend exposes reporting APIs. */
public final class MockRevenue {
    private MockRevenue() {}

    public static int forDay(Calendar day) {
        int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
        int month = day.get(Calendar.MONTH) + 1;
        return 720_000 + ((dayOfMonth * 137 + month * 73) % 780) * 1_000;
    }

    public static int forRange(Calendar from, Calendar to) {
        Calendar cursor = (Calendar) from.clone();
        int total = 0;
        while (!cursor.after(to)) {
            total += forDay(cursor);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return total;
    }

    public static Calendar startOfWeek(Calendar source) {
        Calendar start = (Calendar) source.clone();
        int daysSinceMonday = (start.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
        start.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);
        atStartOfDay(start);
        return start;
    }

    public static Calendar endOfWeek(Calendar source) {
        Calendar end = startOfWeek(source);
        end.add(Calendar.DAY_OF_MONTH, 6);
        return end;
    }

    public static void atStartOfDay(Calendar value) {
        value.set(Calendar.HOUR_OF_DAY, 0);
        value.set(Calendar.MINUTE, 0);
        value.set(Calendar.SECOND, 0);
        value.set(Calendar.MILLISECOND, 0);
    }
}
