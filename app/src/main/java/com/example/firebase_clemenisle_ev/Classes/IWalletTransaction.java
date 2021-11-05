package com.example.firebase_clemenisle_ev.Classes;

public class IWalletTransaction {

    String id, bookingId, timestamp, category, referenceNumber, mobileNumber;
    double value;
    boolean valid = true;

    public IWalletTransaction() {
    }

    public IWalletTransaction(String id, String timestamp, String category, double value) {
        this.id = id;
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

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public boolean isValid() {
        return valid;
    }
}
