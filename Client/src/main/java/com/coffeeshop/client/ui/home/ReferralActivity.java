package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class ReferralActivity extends Activity {

    private static final String REFERRAL_CODE = "COFFEE-QL849";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.referralTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.referralBack).setOnClickListener(v -> finish());

        TextView codeText = findViewById(R.id.referralCodeText);
        codeText.setText(REFERRAL_CODE);

        findViewById(R.id.btnCopyReferralCode).setOnClickListener(v -> copyCodeToClipboard());
        findViewById(R.id.btnShareReferral).setOnClickListener(v -> shareReferralCode());
    }

    private void copyCodeToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Mã giới thiệu Mộc Miên", REFERRAL_CODE);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.referral_code_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReferralCode() {
        String message = "Tham gia Mộc Miên Coffee và nhập mã " + REFERRAL_CODE + " để nhận ngay 20 điểm thưởng khi hoàn tất đơn hàng đầu tiên nhé!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mời dùng Mộc Miên Coffee");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã giới thiệu qua"));
    }
}
