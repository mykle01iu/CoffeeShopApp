package com.coffeeshop.client.model;

public final class UserProfile {
    public final String name;
    public final String phone;
    public final String email;
    public final String birthday;
    public final String gender;
    public final String avatarUri;
    public final int points;
    public final String tier;

    public UserProfile(String name, String phone, String email, String birthday, String gender, String avatarUri, int points, String tier) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.avatarUri = avatarUri;
        this.points = points;
        this.tier = tier;
    }
}
