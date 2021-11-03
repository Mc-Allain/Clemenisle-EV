package com.example.firebase_clemenisle_ev.Classes;

public class ReferenceNumber {

    String id, referenceNumber, timestamp;
    double value;
    boolean valid = true;

    String userId, bookingId;
    boolean removed = true;

    public ReferenceNumber() {
    }

    public ReferenceNumber(String id, String referenceNumber, String timestamp, double value) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public boolean isValid() {
        return valid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
