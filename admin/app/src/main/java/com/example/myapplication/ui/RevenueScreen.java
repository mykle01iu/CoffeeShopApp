package com.example.myapplication.ui;

import android.app.Activity;

import com.example.myapplication.R;

public class RevenueScreen {

    private final Activity activity;
    private final Navigator navigator;

    public RevenueScreen(Activity activity, Navigator navigator) {
        this.activity = activity;
        this.navigator = navigator;
    }

    public void show() {
        activity.setContentView(R.layout.revenue_details);
        activity.findViewById(R.id.back_revenue_dashboard).setOnClickListener(v -> navigator.showDashboard());
    }
}