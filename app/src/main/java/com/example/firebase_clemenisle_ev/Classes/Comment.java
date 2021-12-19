package com.example.firebase_clemenisle_ev.Classes;

public class Comment {
    boolean appealed = false, deactivated = false, fouled = false;
    String id, timestamp, userId  = null, value;

    public Comment() {
    }

    public Comment(String id, String value, String timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public boolean isAppealed() {
        return appealed;
    }

    public void setAppealed(boolean appealed) {
        this.appealed = appealed;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public boolean isFouled() {
        return fouled;
    }

    public void setFouled(boolean fouled) {
        this.fouled = fouled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public void setValue(String value) {
        this.value = value;
    }
}
