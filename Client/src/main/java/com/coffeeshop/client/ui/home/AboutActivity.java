package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.support.SupportInfo;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.snackbar.Snackbar;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        TextView title = findViewById(R.id.aboutTitle);
        if (title != null) {
            title.setTypeface(FontUtils.lora(this, 600));
        }

        View backBtn = findViewById(R.id.aboutBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        TextView versionText = findViewById(R.id.aboutVersionText);
        if (versionText != null) {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionText.setText("Phiên bản " + pInfo.versionName);
            } catch (PackageManager.NameNotFoundException ignored) {
                versionText.setText("Phiên bản 1.0");
            }
        }

        View termsRow = findViewById(R.id.aboutTermsRow);
        if (termsRow != null) {
            termsRow.setOnClickListener(v -> {
                Intent intent = new Intent(this, LegalActivity.class);
                intent.putExtra(LegalActivity.EXTRA_TYPE, "TERMS");
                startActivity(intent);
            });
        }

        View privacyRow = findViewById(R.id.aboutPrivacyRow);
        if (privacyRow != null) {
            privacyRow.setOnClickListener(v -> {
                Intent intent = new Intent(this, LegalActivity.class);
                intent.putExtra(LegalActivity.EXTRA_TYPE, "PRIVACY");
                startActivity(intent);
            });
        }

        // Support Contact Section using SupportInfo
        TextView hotlineText = findViewById(R.id.aboutHotlineText);
        if (hotlineText != null) {
            hotlineText.setText(getString(R.string.support_hotline_desc, SupportInfo.HOTLINE, SupportInfo.HOURS));
        }

        TextView emailText = findViewById(R.id.aboutEmailText);
        if (emailText != null) {
            emailText.setText(SupportInfo.EMAIL);
        }

        View hotlineRow = findViewById(R.id.aboutHotlineRow);
        if (hotlineRow != null) {
            hotlineRow.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SupportInfo.HOTLINE_RAW));
                safeStartActivity(intent);
            });
        }

        View emailRow = findViewById(R.id.aboutEmailRow);
        if (emailRow != null) {
            emailRow.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + SupportInfo.EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
                safeStartActivity(intent);
            });
        }

        View supportRow = findViewById(R.id.aboutSupportRow);
        if (supportRow != null) {
            supportRow.setOnClickListener(v ->
                    startActivity(new Intent(this, SupportActivity.class)));
        }
    }

    private void safeStartActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.support_no_app_toast), Snackbar.LENGTH_SHORT).show();
        }
    }
}
