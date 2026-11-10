package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.UserRepository;
import com.coffeeshop.client.model.UserProfile;
import com.coffeeshop.client.ui.auth.LocalAuthRepository;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends Activity {

    private static final int REQUEST_PICK_AVATAR = 1001;

    private ImageView avatarImage;
    private EditText nameInput, phoneInput, emailInput, birthdayInput;
    private TextView nameError, emailError;
    private RadioGroup genderRadioGroup;
    private RadioButton radioFemale, radioMale, radioOther;
    private Button saveButton;

    private String currentAvatarUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        findViewById(R.id.editProfileBack).setOnClickListener(v -> finish());

        avatarImage = findViewById(R.id.editAvatarImage);
        nameInput = findViewById(R.id.editNameInput);
        phoneInput = findViewById(R.id.editPhoneInput);
        emailInput = findViewById(R.id.editEmailInput);
        birthdayInput = findViewById(R.id.editBirthdayInput);

        nameError = findViewById(R.id.editNameError);
        emailError = findViewById(R.id.editEmailError);

        genderRadioGroup = findViewById(R.id.editGenderRadioGroup);
        radioFemale = findViewById(R.id.radioGenderFemale);
        radioMale = findViewById(R.id.radioGenderMale);
        radioOther = findViewById(R.id.radioGenderOther);

        saveButton = findViewById(R.id.btnSaveProfile);

        findViewById(R.id.btnPickAvatar).setOnClickListener(v -> pickAvatarImage());
        findViewById(R.id.avatarFrame).setOnClickListener(v -> pickAvatarImage());

        birthdayInput.setOnClickListener(v -> showDatePicker());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        nameInput.addTextChangedListener(watcher);
        emailInput.addTextChangedListener(watcher);

        loadCurrentProfile();
        saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadCurrentProfile() {
        UserProfile user = UserRepository.getProfile(this);
        boolean loggedIn = LocalAuthRepository.hasActiveSession(this);

        String currentName = loggedIn ? LocalAuthRepository.getSessionName(this) : user.name;
        if (currentName.isEmpty()) currentName = user.name;
        String currentPhone = loggedIn ? LocalAuthRepository.getSessionIdentifier(this) : user.phone;
        if (currentPhone.isEmpty()) currentPhone = user.phone;

        nameInput.setText(currentName);
        phoneInput.setText(currentPhone);
        emailInput.setText(user.email);
        birthdayInput.setText(user.birthday);

        currentAvatarUri = user.avatarUri;
        if (currentAvatarUri != null && !currentAvatarUri.isEmpty()) {
            try {
                avatarImage.setImageURI(Uri.parse(currentAvatarUri));
            } catch (Exception ignored) { }
        }

        if ("Nam".equalsIgnoreCase(user.gender)) {
            radioMale.setChecked(true);
        } else if ("Khác".equalsIgnoreCase(user.gender)) {
            radioOther.setChecked(true);
        } else {
            radioFemale.setChecked(true);
        }

        validateForm();
    }

    private void pickAvatarImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            currentAvatarUri = imageUri.toString();
            avatarImage.setImageURI(imageUri);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 20;
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String currentBirth = birthdayInput.getText().toString().trim();
        if (currentBirth.contains("/")) {
            try {
                String[] parts = currentBirth.split("/");
                if (parts.length == 3) {
                    day = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]) - 1;
                    year = Integer.parseInt(parts[2]);
                }
            } catch (Exception ignored) { }
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String selected = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            birthdayInput.setText(selected);
        }, year, month, day);

        dialog.show();
    }

    private void validateForm() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            nameError.setText("Vui lòng nhập họ và tên.");
            nameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            nameError.setVisibility(View.GONE);
        }

        if (email.isEmpty()) {
            emailError.setText("Vui lòng nhập địa chỉ email.");
            emailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setText("Email không đúng định dạng.");
            emailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            emailError.setVisibility(View.GONE);
        }

        saveButton.setEnabled(isValid);
    }

    private void saveProfileChanges() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();

        String gender = "Nữ";
        if (radioMale.isChecked()) {
            gender = "Nam";
        } else if (radioOther.isChecked()) {
            gender = "Khác";
        }

        UserProfile oldUser = UserRepository.getProfile(this);
        UserProfile updated = new UserProfile(name, phone, email, birthday, gender, currentAvatarUri, oldUser.points, oldUser.tier);

        UserRepository.saveProfile(this, updated);
        LocalAuthRepository.updateUserProfile(this, name, phone, email);

        Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        finish();
    }
}
