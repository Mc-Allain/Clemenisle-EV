package com.example.firebase_clemenisle_ev.Classes;

public class IWalletTransaction {

    String id, bookingId, timestamp, category, referenceNumber, mobileNumber;
    double value;
    boolean valid = true;

    public IWalletTransaction() {
    }

    public IWalletTransaction(String id, String timestamp, String category, double value, String mobileNumber) {
        this.id = id;
        this.timestamp = timestamp;
        this.category = category;
        this.value = value;
        this.mobileNumber = mobileNumber;
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

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public boolean isValid() {
        return valid;
    }
}
