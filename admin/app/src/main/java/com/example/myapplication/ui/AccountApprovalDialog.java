package com.example.myapplication.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.example.myapplication.auth.AccountStore;

import java.util.List;

public class AccountApprovalDialog {

    private final Activity activity;
    private final AccountStore accounts;
    private AlertDialog dialog;

    public AccountApprovalDialog(Activity activity, AccountStore accounts) {
        this.activity = activity;
        this.accounts = accounts;
    }

    public void show() {
        dismissCurrent();

        if (!accounts.isAdmin()) {
            Toast.makeText(activity, "Bạn không có quyền thực hiện thao tác này.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> pending = accounts.pendingEmails();
        if (pending.isEmpty()) {
            new AlertDialog.Builder(activity)
                    .setTitle("Duyệt tài khoản")
                    .setMessage("Hiện không có tài khoản nào đang chờ phê duyệt.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(UiUtils.dp(activity, 20), UiUtils.dp(activity, 8), UiUtils.dp(activity, 20), UiUtils.dp(activity, 4));

        for (String email : pending) {
            LinearLayout row = new LinearLayout(activity);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, UiUtils.dp(activity, 8), 0, UiUtils.dp(activity, 8));

            LinearLayout details = new LinearLayout(activity);
            details.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(activity);
            name.setText(accounts.nameOf(email));
            name.setTextColor(activity.getColor(R.color.ink));
            name.setTextSize(14);
            name.setTypeface(null, Typeface.BOLD);
            details.addView(name);

            TextView address = new TextView(activity);
            address.setText(email);
            address.setTextColor(activity.getColor(R.color.muted));
            address.setTextSize(12);
            details.addView(address);

            row.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

            Button approve = new Button(activity);
            approve.setText("Duyệt");
            approve.setOnClickListener(v -> {
                accounts.approve(email);
                Toast.makeText(activity, "Đã phê duyệt " + email, Toast.LENGTH_SHORT).show();
                show(); // làm mới danh sách
            });
            row.addView(approve, new LinearLayout.LayoutParams(-2, UiUtils.dp(activity, 44)));

            list.addView(row);
        }

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(list);

        dialog = new AlertDialog.Builder(activity)
                .setTitle("Tài khoản chờ phê duyệt")
                .setView(scroll)
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void dismissCurrent() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}