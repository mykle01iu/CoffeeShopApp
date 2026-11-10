package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.SettingsRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class SettingsActivity extends Activity {

    private Switch switchPromo;
    private RadioGroup themeRadioGroup, langRadioGroup;
    private RadioButton radioLight, radioDark, radioSystem;
    private RadioButton radioVi, radioEn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.settingsTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.settingsBack).setOnClickListener(v -> finish());

        switchPromo = findViewById(R.id.switchPromoNotification);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        radioLight = findViewById(R.id.radioThemeLight);
        radioDark = findViewById(R.id.radioThemeDark);
        radioSystem = findViewById(R.id.radioThemeSystem);

        langRadioGroup = findViewById(R.id.langRadioGroup);
        radioVi = findViewById(R.id.radioLangVi);
        radioEn = findViewById(R.id.radioLangEn);

        loadSettings();

        switchPromo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsRepository.setPromoNotificationsEnabled(this, isChecked);
            Toast.makeText(this, isChecked ? "Đã bật thông báo khuyến mãi" : "Đã tắt thông báo khuyến mãi", Toast.LENGTH_SHORT).show();
        });

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = 1; // Light
            if (checkedId == R.id.radioThemeDark) {
                mode = 2; // Dark
            } else if (checkedId == R.id.radioThemeSystem) {
                mode = 0; // System
            }
            SettingsRepository.setThemeMode(this, mode);
        });

        langRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = checkedId == R.id.radioLangEn ? "en" : "vi";
            SettingsRepository.setLanguage(this, lang);
            Toast.makeText(this, "Đã lưu cài đặt ngôn ngữ", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSettings() {
        switchPromo.setChecked(SettingsRepository.isPromoNotificationsEnabled(this));

        int themeMode = SettingsRepository.getThemeMode(this);
        if (themeMode == 2) {
            radioDark.setChecked(true);
        } else if (themeMode == 0) {
            radioSystem.setChecked(true);
        } else {
            radioLight.setChecked(true);
        }

        String lang = SettingsRepository.getLanguage(this);
        if ("en".equalsIgnoreCase(lang)) {
            radioEn.setChecked(true);
        } else {
            radioVi.setChecked(true);
        }
    }
}
