package com.example.myapplication.model;

/** Profile fields returned by the administrator user directory API. */
public class UserProfile {
    public final int id;
    public final String name;
    public final String email;
    public final String role;
    public final String createdAt;

    public UserProfile(int id, String name, String email, String role, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }
}
