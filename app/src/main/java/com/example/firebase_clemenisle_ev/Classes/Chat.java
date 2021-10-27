package com.example.firebase_clemenisle_ev.Classes;

public class Chat {

    String senderId, message;

    public Chat() {
    }

    public Chat(String senderId, String message) {
        this.senderId = senderId;
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }
}
