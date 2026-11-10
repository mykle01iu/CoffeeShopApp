package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.coffeeshop.client.model.UserProfile;

public final class UserRepository {
    private static final String PREF_NAME = "moc_mien_user_profile";

    private UserRepository() { }

    public static UserProfile getProfile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString("name", "Quynh Luong");
        String phone = prefs.getString("phone", "0849396218");
        String email = prefs.getString("email", "quynhluong@gmail.com");
        String birthday = prefs.getString("birthday", "15/08/2000");
        String gender = prefs.getString("gender", "Nữ");
        String avatarUri = prefs.getString("avatarUri", "");
        int points = prefs.getInt("points", 120);
        String tier = prefs.getString("tier", "Thân Thiết");

        return new UserProfile(name, phone, email, birthday, gender, avatarUri, points, tier);
    }

    public static void saveProfile(Context context, UserProfile profile) {
        if (profile == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString("name", profile.name)
                .putString("phone", profile.phone)
                .putString("email", profile.email)
                .putString("birthday", profile.birthday)
                .putString("gender", profile.gender)
                .putString("avatarUri", profile.avatarUri)
                .putInt("points", profile.points)
                .putString("tier", profile.tier)
                .apply();
    }
}
