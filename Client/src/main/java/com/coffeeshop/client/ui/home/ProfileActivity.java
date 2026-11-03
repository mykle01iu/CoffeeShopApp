package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.ui.auth.AuthChoiceActivity;
import com.coffeeshop.client.ui.auth.LoginActivity;
import com.coffeeshop.client.ui.auth.LocalAuthRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class ProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));
        ((TextView) findViewById(R.id.profileTitle)).setTypeface(FontUtils.lora(this, 500));
        findViewById(R.id.profileBackButton).setOnClickListener(view -> finish());
        findViewById(R.id.profileLoginButton).setOnClickListener(view -> openAccountSignIn());
        findViewById(R.id.profileAddressButton).setOnClickListener(view ->
                startActivity(new Intent(this, AddressActivity.class)));
        findViewById(R.id.profileLogoutButton).setOnClickListener(view -> {
            LocalAuthRepository.logout(this);
            renderProfile();
        });
        renderProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderProfile();
    }

    private void renderProfile() {
        if (findViewById(R.id.profileStatusText) == null) return;
        boolean loggedIn = LocalAuthRepository.hasActiveSession(this);
        String savedIdentifier = LocalAuthRepository.getLastRegisteredIdentifier(this);
        String identifierValue = loggedIn ? LocalAuthRepository.getSessionIdentifier(this) : savedIdentifier;
        String nameValue = loggedIn ? LocalAuthRepository.getSessionName(this)
                : LocalAuthRepository.getRegisteredName(this, savedIdentifier);
        boolean hasSavedAccount = !identifierValue.isEmpty() && !nameValue.isEmpty();
        TextView status = findViewById(R.id.profileStatusText);
        TextView name = findViewById(R.id.profileNameValue);
        TextView identifier = findViewById(R.id.profileIdentifierValue);
        View details = findViewById(R.id.profileInfoCard);
        View login = findViewById(R.id.profileLoginButton);
        View logout = findViewById(R.id.profileLogoutButton);
        if (hasSavedAccount) {
            status.setText(loggedIn ? R.string.profile_logged_in_status : R.string.profile_saved_account_status);
            name.setText(nameValue);
            identifier.setText(identifierValue);
            details.setVisibility(View.VISIBLE);
            login.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            logout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            ((android.widget.Button) login).setText(R.string.profile_login_existing);
        } else {
            status.setText(R.string.profile_login_prompt);
            details.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
            ((android.widget.Button) login).setText(R.string.profile_login_action);
        }
    }

    private void openAccountSignIn() {
        if (!LocalAuthRepository.getLastRegisteredIdentifier(this).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LocalAuthRepository.EXTRA_IDENTIFIER,
                    LocalAuthRepository.getLastRegisteredIdentifier(this));
            startActivity(intent);
        } else {
            startActivity(new Intent(this, AuthChoiceActivity.class));
        }
    }
}
