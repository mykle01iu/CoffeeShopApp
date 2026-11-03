package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class AddressActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.addressTitle)).setTypeface(FontUtils.lora(this, 500));
        EditText addressInput = findViewById(R.id.addressInput);
        TextView error = findViewById(R.id.addressError);
        addressInput.setText(getSharedPreferences("moc_mien_profile", MODE_PRIVATE)
                .getString("delivery_address", ""));
        findViewById(R.id.addressBackButton).setOnClickListener(view -> finish());
        Button saveButton = findViewById(R.id.addressSaveButton);
        saveButton.setOnClickListener(view -> {
            String address = addressInput.getText().toString().trim();
            if (address.isEmpty()) {
                error.setText(R.string.address_required);
                error.setVisibility(android.view.View.VISIBLE);
                addressInput.requestFocus();
                return;
            }
            getSharedPreferences("moc_mien_profile", MODE_PRIVATE).edit()
                    .putString("delivery_address", address).apply();
            android.widget.Toast.makeText(this, R.string.address_saved, android.widget.Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
