package com.example.firebase_clemenisle_ev.Classes;

public class IWalletTransaction {

    String id, bookingId, timestamp, category;
    double value;

    public IWalletTransaction() {
    }

    public IWalletTransaction(String id, String bookingId, String timestamp, String category, double value) {
        this.id = id;
        this.bookingId = bookingId;
        this.timestamp = timestamp;
        this.category = category;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }

    public double getValue() {
        return value;
    }
}
