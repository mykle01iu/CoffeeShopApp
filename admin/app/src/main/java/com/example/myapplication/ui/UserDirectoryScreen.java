package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.data.BackendApi;
import com.example.myapplication.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin user directory with client-side name/email search. */
public class UserDirectoryScreen {
    private final Activity activity;
    private final Navigator navigator;
    private final BackendApi api;
    private final List<UserProfile> users = new ArrayList<>();
    private LinearLayout userList;
    private TextView summary;
    private EditText search;
    private String loadError;
    private boolean loading;

    public UserDirectoryScreen(Activity activity, Navigator navigator, BackendApi api) {
        this.activity = activity;
        this.navigator = navigator;
        this.api = api;
    }

    public void show() {
        ScrollView scroll = new ScrollView(activity);
        scroll.setId(R.id.user_directory_screen);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(248, 246, 241));
        LinearLayout root = column();
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(root);

        LinearLayout header = row();
        TextView back = text("‹", 32, Color.rgb(46, 36, 31), false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(shape(Color.WHITE, 14));
        header.addView(back, params(dp(44), dp(44)));
        back.setOnClickListener(v -> navigator.showDashboard());
        LinearLayout heading = column();
        heading.addView(text("QUẢN LÝ CỬA HÀNG", 10, Color.rgb(112, 69, 47), true));
        heading.addView(text("Danh sách người dùng", 23, Color.rgb(46, 36, 31), true));
        LinearLayout.LayoutParams headingParams = params(0, -2);
        headingParams.leftMargin = dp(12);
        headingParams.weight = 1;
        header.addView(heading, headingParams);
        root.addView(header);

        summary = text("Đang tải danh sách người dùng...", 13, Color.rgb(136, 125, 117), false);
        LinearLayout.LayoutParams summaryParams = params(-1, -2);
        summaryParams.topMargin = dp(9);
        root.addView(summary, summaryParams);

        search = new EditText(activity);
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setTextColor(Color.rgb(46, 36, 31));
        search.setHint("Tìm theo tên hoặc email");
        search.setPadding(dp(15), dp(12), dp(15), dp(12));
        search.setBackground(shape(Color.WHITE, 14));
        LinearLayout.LayoutParams searchParams = params(-1, -2);
        searchParams.topMargin = dp(17);
        root.addView(search, searchParams);

        userList = column();
        LinearLayout.LayoutParams listParams = params(-1, -2);
        listParams.topMargin = dp(14);
        root.addView(userList, listParams);

        activity.setContentView(scroll);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderUsers(); }
            @Override public void afterTextChanged(Editable editable) { }
        });
        loadUsers();
    }

    private void loadUsers() {
        loading = true;
        loadError = null;
        if (summary != null) summary.setText("Đang tải danh sách người dùng...");
        api.getAdminUsers(new BackendApi.Callback<List<UserProfile>>() {
            @Override public void onSuccess(List<UserProfile> result) {
                if (!isVisible()) return;
                loading = false;
                users.clear();
                if (result != null) users.addAll(result);
                renderUsers();
            }

            @Override public void onError(BackendApi.ApiError error) {
                if (!isVisible()) return;
                loading = false;
                loadError = error.message;
                renderUsers();
            }
        });
    }

    private boolean isVisible() {
        return activity.findViewById(R.id.user_directory_screen) != null;
    }

    private void renderUsers() {
        if (userList == null || !isVisible()) return;
        userList.removeAllViews();
        if (loading) {
            summary.setText("Đang tải danh sách người dùng...");
            return;
        }
        if (loadError != null) {
            summary.setText("Không tải được danh sách người dùng");
            userList.addView(text("Lỗi: " + loadError, 13, Color.rgb(160, 54, 48), false));
            TextView retry = text("Thử lại", 13, Color.WHITE, true);
            retry.setGravity(Gravity.CENTER);
            retry.setPadding(0, dp(11), 0, dp(11));
            retry.setBackground(shape(Color.rgb(112, 69, 47), 12));
            LinearLayout.LayoutParams retryParams = params(-1, -2);
            retryParams.topMargin = dp(12);
            userList.addView(retry, retryParams);
            retry.setOnClickListener(v -> {
                summary.setText("Đang tải danh sách người dùng...");
                userList.removeAllViews();
                loadUsers();
            });
            return;
        }
        if (users.isEmpty()) {
            summary.setText("0 người dùng");
            addMessage("Danh sách trống");
            return;
        }

        String query = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<UserProfile> matches = new ArrayList<>();
        for (UserProfile user : users) {
            if (user.name.toLowerCase(Locale.ROOT).contains(query)
                    || user.email.toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(user);
            }
        }
        summary.setText(query.isEmpty()
                ? users.size() + " người dùng"
                : matches.size() + "/" + users.size() + " người dùng phù hợp");
        if (matches.isEmpty()) {
            addMessage("Không tìm thấy người dùng phù hợp");
            return;
        }
        for (UserProfile user : matches) addUserCard(user);
    }

    private void addUserCard(UserProfile user) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(shape(Color.WHITE, 16));
        LinearLayout.LayoutParams cardParams = params(-1, -2);
        cardParams.bottomMargin = dp(10);
        userList.addView(card, cardParams);

        LinearLayout top = row();
        TextView name = text(user.name, 15, Color.rgb(46, 36, 31), true);
        LinearLayout.LayoutParams nameParams = params(0, -2);
        nameParams.weight = 1;
        top.addView(name, nameParams);
        TextView role = text(user.role, 10, Color.rgb(112, 69, 47), true);
        role.setPadding(dp(9), dp(6), dp(9), dp(6));
        role.setBackground(shape(Color.rgb(248, 239, 230), 18));
        top.addView(role);
        card.addView(top);

        TextView email = text(user.email, 13, Color.rgb(93, 82, 74), false);
        LinearLayout.LayoutParams emailParams = params(-1, -2);
        emailParams.topMargin = dp(8);
        card.addView(email, emailParams);
        if (!user.createdAt.isEmpty()) {
            TextView created = text("Tham gia: " + user.createdAt.replace('T', ' '),
                    11, Color.rgb(136, 125, 117), false);
            LinearLayout.LayoutParams createdParams = params(-1, -2);
            createdParams.topMargin = dp(6);
            card.addView(created, createdParams);
        }
    }

    private void addMessage(String message) {
        TextView empty = text(message, 14, Color.rgb(136, 125, 117), false);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(12), dp(30), dp(12), dp(30));
        empty.setBackground(shape(Color.WHITE, 16));
        userList.addView(empty, params(-1, -2));
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable shape(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) { return UiUtils.dp(activity, value); }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }
}
