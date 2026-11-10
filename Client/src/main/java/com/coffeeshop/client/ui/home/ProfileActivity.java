package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.PointsRepository;
import com.coffeeshop.client.data.UserRepository;
import com.coffeeshop.client.data.VoucherRepository;
import com.coffeeshop.client.model.UserProfile;
import com.coffeeshop.client.ui.auth.AuthChoiceActivity;
import com.coffeeshop.client.ui.auth.LoginActivity;
import com.coffeeshop.client.ui.auth.LocalAuthRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        ((TextView) findViewById(R.id.profileTitle)).setTypeface(FontUtils.lora(this, 600));

        findViewById(R.id.profileBackButton).setOnClickListener(view -> finish());

        // Edit Profile
        findViewById(R.id.profileEditButton).setOnClickListener(view ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        // Points Card & History Sheet
        findViewById(R.id.profilePointsCard).setOnClickListener(view -> showPointsHistorySheet());

        // Menu Rows - Group 1: Đơn hàng & ưu đãi
        findViewById(R.id.profileOrdersRow).setOnClickListener(view ->
                startActivity(new Intent(this, OrdersActivity.class)));

        findViewById(R.id.profileVouchersRow).setOnClickListener(view -> showVouchersSheet());

        findViewById(R.id.profileFavoritesRow).setOnClickListener(view ->
                startActivity(new Intent(this, FavoritesActivity.class)));

        findViewById(R.id.profileMyReviewsRow).setOnClickListener(view -> showComingSoonToast("Đánh giá của tôi"));

        // Menu Rows - Group 2: Cá nhân
        findViewById(R.id.profileAddressRow).setOnClickListener(view ->
                startActivity(new Intent(this, AddressActivity.class)));

        findViewById(R.id.profilePaymentMethodRow).setOnClickListener(view -> showComingSoonToast("Phương thức thanh toán"));

        findViewById(R.id.profileReferralRow).setOnClickListener(view ->
                startActivity(new Intent(this, ReferralActivity.class)));

        // Menu Rows - Group 3: Bảo mật & cài đặt
        findViewById(R.id.profileChangePasswordRow).setOnClickListener(view ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        findViewById(R.id.profileSettingsRow).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.profileSupportRow).setOnClickListener(view ->
                startActivity(new Intent(this, SupportActivity.class)));

        findViewById(R.id.profileAboutRow).setOnClickListener(view ->
                startActivity(new Intent(this, AboutActivity.class)));

        // Login / Logout buttons
        findViewById(R.id.profileLoginActionButton).setOnClickListener(view -> openAccountSignIn());

        findViewById(R.id.profileLogoutButton).setOnClickListener(view -> showLogoutConfirmationDialog());

        renderProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderProfile();
    }

    private void renderProfile() {
        boolean loggedIn = LocalAuthRepository.hasActiveSession(this);
        UserProfile user = UserRepository.getProfile(this);

        String savedIdentifier = LocalAuthRepository.getLastRegisteredIdentifier(this);
        String identifierValue = loggedIn ? LocalAuthRepository.getSessionIdentifier(this) : (savedIdentifier.isEmpty() ? user.phone : savedIdentifier);
        String nameValue = loggedIn ? LocalAuthRepository.getSessionName(this) : user.name;
        if (nameValue.isEmpty()) nameValue = user.name;
        if (identifierValue.isEmpty()) identifierValue = user.phone;

        boolean hasAccount = loggedIn || !identifierValue.isEmpty();

        TextView nameText = findViewById(R.id.profileNameText);
        TextView identifierText = findViewById(R.id.profileIdentifierText);
        TextView pointsText = findViewById(R.id.profilePointsText);
        TextView tierText = findViewById(R.id.profileTierText);
        TextView voucherBadge = findViewById(R.id.profileVoucherBadgeText);
        ProgressBar pointsProgressBar = findViewById(R.id.profilePointsProgressBar);
        ImageView avatarImage = findViewById(R.id.profileAvatarImage);

        View loginButton = findViewById(R.id.profileLoginActionButton);
        View logoutButton = findViewById(R.id.profileLogoutButton);

        if (hasAccount) {
            nameText.setText(nameValue);
            identifierText.setText(identifierValue);
            loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            logoutButton.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        } else {
            nameText.setText(user.name);
            identifierText.setText(user.phone);
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }

        if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
            try {
                avatarImage.setImageURI(Uri.parse(user.avatarUri));
            } catch (Exception ignored) { }
        }

        int points = PointsRepository.getTotalPoints();
        String tier = PointsRepository.getTier();
        pointsText.setText(points + " điểm (Hạng " + tier + ")");
        if (pointsProgressBar != null) {
            pointsProgressBar.setMax(200);
            pointsProgressBar.setProgress(Math.min(200, points));
        }
        int remaining = PointsRepository.getRemainingToNextTier();
        if (remaining > 0) {
            tierText.setText("Còn " + remaining + " điểm nữa lên Hạng Vàng");
        } else {
            tierText.setText("Bạn đã đạt Hạng Vàng cao nhất");
        }

        if (voucherBadge != null) {
            int activeVouchers = VoucherRepository.getActiveCount();
            voucherBadge.setText(activeVouchers + " mã ›");
        }
    }

    private void showComingSoonToast(String featureName) {
        Toast.makeText(this, featureName + " - Tính năng sắp ra mắt", Toast.LENGTH_SHORT).show();
    }

    private void showPointsHistorySheet() {
        PointsHistoryBottomSheet sheet = new PointsHistoryBottomSheet();
        sheet.show(getSupportFragmentManager(), "PointsHistoryBottomSheet");
    }

    private void showVouchersSheet() {
        VoucherBottomSheet sheet = new VoucherBottomSheet();
        sheet.show(getSupportFragmentManager(), "VoucherBottomSheet");
    }



    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    LocalAuthRepository.logout(this);
                    renderProfile();
                    Toast.makeText(this, "Đã đăng xuất tài khoản thành công.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
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
