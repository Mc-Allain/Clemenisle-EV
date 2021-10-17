package com.example.firebase_clemenisle_ev.Classes;

public class Comment {
    boolean appealed = false, deactivated = false, fouled = false;
    String id, userId, value;

    public Comment() {
    }

    public Comment(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public boolean isAppealed() {
        return appealed;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public boolean isFouled() {
        return fouled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getValue() {
        return value;
    }
}
