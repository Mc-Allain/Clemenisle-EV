package com.example.firebase_clemenisle_ev.Classes;

public class OtherComment {
    String senderUserId = null, spotId = null;

    public OtherComment() {
    }

    public OtherComment(String senderUserId, String spotId) {
        this.senderUserId = senderUserId;
        this.spotId = spotId;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public String getSpotId() {
        return spotId;
    }
}
