package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.data.MockRevenue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Revenue overview with daily, weekly, and monthly mock reporting. */
public class RevenueScreen {
    private static final int BROWN = Color.rgb(112, 69, 47);
    private static final String[] TABS = {"Ngày", "Tuần", "Tháng"};

    private final Activity activity;
    private final Navigator navigator;
    private String period = "Ngày";
    private Calendar anchor = Calendar.getInstance(new Locale("vi", "VN"));
    private int expandedWeek = -1;
    private LinearLayout content;

    public RevenueScreen(Activity activity, Navigator navigator) {
        this.activity = activity;
        this.navigator = navigator;
    }

    public void show() { render(); }

    private void render() {
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(OrderUi.BG);
        LinearLayout root = OrderUi.column(activity);
        root.setPadding(OrderUi.dp(activity, 20), OrderUi.dp(activity, 18), OrderUi.dp(activity, 20), OrderUi.dp(activity, 28));
        scroll.addView(root);

        LinearLayout header = OrderUi.row(activity);
        TextView back = OrderUi.text(activity, "‹", 32, OrderUi.INK, false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(OrderUi.shape(Color.WHITE, OrderUi.dp(activity, 14)));
        header.addView(back, OrderUi.params(OrderUi.dp(activity, 44), OrderUi.dp(activity, 44)));
        back.setOnClickListener(v -> navigator.showDashboard());
        LinearLayout heading = OrderUi.column(activity);
        heading.addView(OrderUi.text(activity, "MỘC MIÊN  /  QUẢN TRỊ", 10, BROWN, true));
        heading.addView(OrderUi.text(activity, "Quản lý doanh thu", 21, OrderUi.INK, true));
        LinearLayout.LayoutParams headingParams = OrderUi.params(0, -2);
        headingParams.weight = 1;
        headingParams.leftMargin = OrderUi.dp(activity, 12);
        header.addView(heading, headingParams);
        root.addView(header);

        TextView mockLabel = OrderUi.text(activity, "Số liệu minh họa · chưa kết nối backend", 12, OrderUi.MUTED, false);
        LinearLayout.LayoutParams mockParams = OrderUi.params(-2, -2);
        mockParams.topMargin = OrderUi.dp(activity, 8);
        root.addView(mockLabel, mockParams);

        LinearLayout tabs = OrderUi.row(activity);
        tabs.setPadding(OrderUi.dp(activity, 4), OrderUi.dp(activity, 4), OrderUi.dp(activity, 4), OrderUi.dp(activity, 4));
        tabs.setBackground(OrderUi.shape(Color.rgb(239, 234, 227), OrderUi.dp(activity, 14)));
        LinearLayout.LayoutParams tabsParams = OrderUi.params(-1, -2);
        tabsParams.topMargin = OrderUi.dp(activity, 18);
        root.addView(tabs, tabsParams);
        for (String tab : TABS) {
            TextView item = OrderUi.text(activity, tab, 13, tab.equals(period) ? BROWN : OrderUi.MUTED, true);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, OrderUi.dp(activity, 10), 0, OrderUi.dp(activity, 10));
            if (tab.equals(period)) item.setBackground(OrderUi.shape(Color.WHITE, OrderUi.dp(activity, 11)));
            LinearLayout.LayoutParams p = OrderUi.params(0, -2);
            p.weight = 1;
            tabs.addView(item, p);
            item.setOnClickListener(v -> { period = tab; expandedWeek = -1; render(); });
        }

        content = OrderUi.column(activity);
        LinearLayout.LayoutParams contentParams = OrderUi.params(-1, -2);
        contentParams.topMargin = OrderUi.dp(activity, 18);
        root.addView(content, contentParams);
        if ("Ngày".equals(period)) renderDay();
        else if ("Tuần".equals(period)) renderWeek();
        else renderMonth();
        activity.setContentView(scroll);
    }

    private void renderDay() {
        Calendar day = (Calendar) anchor.clone();
        setPeriodNavigator("Ngày đang xem", format(day, "EEEE, dd/MM/yyyy"), () -> shiftAnchor(Calendar.DAY_OF_MONTH, -1), () -> shiftAnchor(Calendar.DAY_OF_MONTH, 1));
        addTotalCard("Doanh thu trong ngày", MockRevenue.forDay(day));
        TextView context = OrderUi.text(activity, "Doanh thu mẫu của ngày đã chọn", 12, OrderUi.MUTED, false);
        LinearLayout.LayoutParams p = OrderUi.params(-2, -2);
        p.topMargin = OrderUi.dp(activity, 12);
        content.addView(context, p);
    }

    private void renderWeek() {
        Calendar start = MockRevenue.startOfWeek(anchor);
        Calendar end = MockRevenue.endOfWeek(anchor);
        if (sameWeek(anchor, Calendar.getInstance())) end = minDate(end, Calendar.getInstance());
        setPeriodNavigator("Tuần đang xem", format(start, "dd/MM") + " – " + format(end, "dd/MM/yyyy"),
                () -> shiftAnchor(Calendar.WEEK_OF_YEAR, -1), () -> shiftAnchor(Calendar.WEEK_OF_YEAR, 1));
        addTotalCard("Tổng doanh thu tuần", MockRevenue.forRange(start, end));
        addSectionHeading("Chi tiết từng ngày");
        Calendar cursor = (Calendar) start.clone();
        int dayCount = 0;
        while (!cursor.after(end) && dayCount < 7) {
            addRevenueRow(format(cursor, "EEEE, dd/MM"), MockRevenue.forDay(cursor), null);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
            dayCount++;
        }
    }

    private void renderMonth() {
        Calendar monthStart = (Calendar) anchor.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        MockRevenue.atStartOfDay(monthStart);
        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (sameMonth(anchor, Calendar.getInstance())) monthEnd = minDate(monthEnd, Calendar.getInstance());
        setPeriodNavigator("Tháng đang xem", format(monthStart, "MMMM yyyy"),
                () -> shiftAnchor(Calendar.MONTH, -1), () -> shiftAnchor(Calendar.MONTH, 1));
        addTotalCard("Tổng doanh thu tháng", MockRevenue.forRange(monthStart, monthEnd));
        addSectionHeading("Doanh thu theo tuần · chạm để xem từng ngày");

        List<Calendar[]> weeks = weeksInMonth(monthStart, monthEnd);
        for (int i = 0; i < weeks.size(); i++) {
            Calendar[] range = weeks.get(i);
            final int index = i;
            LinearLayout weekCard = OrderUi.card(activity);
            LinearLayout row = OrderUi.row(activity);
            LinearLayout labels = OrderUi.column(activity);
            labels.addView(OrderUi.text(activity, "Tuần " + (i + 1), 14, OrderUi.INK, true));
            TextView dates = OrderUi.text(activity, format(range[0], "dd/MM") + " – " + format(range[1], "dd/MM"), 11, OrderUi.MUTED, false);
            LinearLayout.LayoutParams datesParams = OrderUi.params(-2, -2);
            datesParams.topMargin = OrderUi.dp(activity, 4);
            labels.addView(dates, datesParams);
            LinearLayout.LayoutParams labelsParams = OrderUi.params(0, -2);
            labelsParams.weight = 1;
            row.addView(labels, labelsParams);
            row.addView(OrderUi.text(activity, OrderUi.money(MockRevenue.forRange(range[0], range[1])), 13, BROWN, true));
            weekCard.addView(row);

            if (expandedWeek == i) {
                View divider = new View(activity);
                divider.setBackgroundColor(Color.rgb(238, 232, 225));
                LinearLayout.LayoutParams divParams = OrderUi.params(-1, OrderUi.dp(activity, 1));
                divParams.topMargin = OrderUi.dp(activity, 12);
                weekCard.addView(divider, divParams);
                Calendar cursor = (Calendar) range[0].clone();
                while (!cursor.after(range[1])) {
                    LinearLayout daily = OrderUi.row(activity);
                    TextView dayLabel = OrderUi.text(activity, format(cursor, "EEE, dd/MM"), 12, OrderUi.MUTED, false);
                    LinearLayout.LayoutParams labelParams = OrderUi.params(0, -2);
                    labelParams.weight = 1;
                    daily.addView(dayLabel, labelParams);
                    daily.addView(OrderUi.text(activity, OrderUi.money(MockRevenue.forDay(cursor)), 12, OrderUi.INK, true));
                    LinearLayout.LayoutParams dailyParams = OrderUi.params(-1, -2);
                    dailyParams.topMargin = OrderUi.dp(activity, 10);
                    weekCard.addView(daily, dailyParams);
                    cursor.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            weekCard.setClickable(true);
            weekCard.setFocusable(true);
            weekCard.setOnClickListener(v -> { expandedWeek = expandedWeek == index ? -1 : index; render(); });
            LinearLayout.LayoutParams cardParams = OrderUi.params(-1, -2);
            cardParams.bottomMargin = OrderUi.dp(activity, 9);
            content.addView(weekCard, cardParams);
        }
    }

    private List<Calendar[]> weeksInMonth(Calendar monthStart, Calendar monthEnd) {
        List<Calendar[]> ranges = new ArrayList<>();
        Calendar cursor = (Calendar) monthStart.clone();
        while (!cursor.after(monthEnd)) {
            Calendar weekEnd = MockRevenue.endOfWeek(cursor);
            if (weekEnd.after(monthEnd)) weekEnd = (Calendar) monthEnd.clone();
            ranges.add(new Calendar[]{(Calendar) cursor.clone(), weekEnd});
            cursor = (Calendar) weekEnd.clone();
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return ranges;
    }

    private void setPeriodNavigator(String label, String value, Runnable previous, Runnable next) {
        LinearLayout card = OrderUi.card(activity);
        LinearLayout row = OrderUi.row(activity);
        TextView prev = arrow("‹");
        row.addView(prev, OrderUi.params(OrderUi.dp(activity, 36), OrderUi.dp(activity, 40)));
        prev.setOnClickListener(v -> previous.run());
        LinearLayout center = OrderUi.column(activity);
        center.setGravity(Gravity.CENTER);
        center.addView(OrderUi.text(activity, label, 11, OrderUi.MUTED, false));
        TextView periodValue = OrderUi.text(activity, value, 14, OrderUi.INK, true);
        periodValue.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams valueParams = OrderUi.params(-1, -2);
        valueParams.topMargin = OrderUi.dp(activity, 4);
        center.addView(periodValue, valueParams);
        LinearLayout.LayoutParams centerParams = OrderUi.params(0, -2);
        centerParams.weight = 1;
        row.addView(center, centerParams);
        TextView forward = arrow("›");
        row.addView(forward, OrderUi.params(OrderUi.dp(activity, 36), OrderUi.dp(activity, 40)));
        forward.setOnClickListener(v -> next.run());
        card.addView(row);
        content.addView(card);
    }

    private TextView arrow(String glyph) {
        TextView arrow = OrderUi.text(activity, glyph, 26, BROWN, false);
        arrow.setGravity(Gravity.CENTER);
        arrow.setBackground(OrderUi.shape(Color.rgb(255, 248, 239), OrderUi.dp(activity, 12)));
        arrow.setClickable(true);
        arrow.setFocusable(true);
        return arrow;
    }

    private void addTotalCard(String title, int amount) {
        LinearLayout card = OrderUi.column(activity);
        card.setPadding(OrderUi.dp(activity, 18), OrderUi.dp(activity, 18), OrderUi.dp(activity, 18), OrderUi.dp(activity, 18));
        GradientDrawable background = new GradientDrawable();
        background.setColors(new int[]{Color.rgb(112, 69, 47), Color.rgb(145, 97, 67)});
        background.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        background.setCornerRadius(OrderUi.dp(activity, 18));
        card.setBackground(background);
        LinearLayout.LayoutParams p = OrderUi.params(-1, -2);
        p.topMargin = OrderUi.dp(activity, 12);
        content.addView(card, p);
        card.addView(OrderUi.text(activity, title.toUpperCase(new Locale("vi", "VN")), 11, Color.rgb(238, 224, 211), true));
        TextView total = OrderUi.text(activity, OrderUi.money(amount), 28, Color.WHITE, true);
        LinearLayout.LayoutParams totalParams = OrderUi.params(-2, -2);
        totalParams.topMargin = OrderUi.dp(activity, 7);
        card.addView(total, totalParams);
    }

    private void addSectionHeading(String title) {
        TextView heading = OrderUi.text(activity, title, 15, OrderUi.INK, true);
        LinearLayout.LayoutParams p = OrderUi.params(-1, -2);
        p.topMargin = OrderUi.dp(activity, 22);
        p.bottomMargin = OrderUi.dp(activity, 10);
        content.addView(heading, p);
    }

    private void addRevenueRow(String title, int amount, Runnable click) {
        LinearLayout card = OrderUi.card(activity);
        LinearLayout row = OrderUi.row(activity);
        TextView date = OrderUi.text(activity, title, 13, OrderUi.INK, true);
        LinearLayout.LayoutParams dateParams = OrderUi.params(0, -2);
        dateParams.weight = 1;
        row.addView(date, dateParams);
        row.addView(OrderUi.text(activity, OrderUi.money(amount), 13, BROWN, true));
        card.addView(row);
        if (click != null) card.setOnClickListener(v -> click.run());
        LinearLayout.LayoutParams p = OrderUi.params(-1, -2);
        p.bottomMargin = OrderUi.dp(activity, 8);
        content.addView(card, p);
    }

    private void shiftAnchor(int field, int amount) {
        anchor.add(field, amount);
        Calendar today = Calendar.getInstance();
        if (anchor.after(today)) anchor = today;
        expandedWeek = -1;
        render();
    }

    private boolean sameWeek(Calendar first, Calendar second) {
        return MockRevenue.startOfWeek(first).get(Calendar.DAY_OF_YEAR) == MockRevenue.startOfWeek(second).get(Calendar.DAY_OF_YEAR)
                && MockRevenue.startOfWeek(first).get(Calendar.YEAR) == MockRevenue.startOfWeek(second).get(Calendar.YEAR);
    }

    private boolean sameMonth(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) && first.get(Calendar.MONTH) == second.get(Calendar.MONTH);
    }

    private Calendar minDate(Calendar first, Calendar second) {
        return first.before(second) ? first : (Calendar) second.clone();
    }

    private String format(Calendar date, String pattern) {
        return new SimpleDateFormat(pattern, new Locale("vi", "VN")).format(date.getTime());
    }
}
