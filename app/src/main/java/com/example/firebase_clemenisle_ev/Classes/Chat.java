package com.example.firebase_clemenisle_ev.Classes;

public class Chat {

    String id, senderId, message, timestamp;

    public Chat() {
    }

    public Chat(String id, String senderId, String message, String timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
