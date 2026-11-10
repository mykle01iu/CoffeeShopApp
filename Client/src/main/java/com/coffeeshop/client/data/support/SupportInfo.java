package com.coffeeshop.client.data.support;

import java.util.Calendar;

public final class SupportInfo {
    public static final String HOTLINE = "1900 6868";
    public static final String HOTLINE_RAW = "19006868";
    public static final String HOURS = "07:00 - 22:00";
    public static final String EMAIL = "cskh@mocmiencoffee.com";
    public static final String FANPAGE_NAME = "Mộc Miên Coffee Official";
    public static final String FANPAGE_URL = "https://facebook.com/mocmiencoffee";
    public static final String ADDRESS = "123 Đường Cà Phê, Quận 1, TP.HCM";

    private SupportInfo() { }

    public static boolean isSupportOpen() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 7 && hour < 22;
    }
}
