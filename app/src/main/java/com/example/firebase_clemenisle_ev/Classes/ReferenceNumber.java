package com.example.firebase_clemenisle_ev.Classes;

public class ReferenceNumber {

    String id, referenceNumber, timestamp;
    double value;
    boolean valid = true, iWalletUsed = false, notified = true;

    public ReferenceNumber() {
    }

    public ReferenceNumber(String id, String timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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

    public boolean isiWalletUsed() {
        return iWalletUsed;
    }

    public void setiWalletUsed(boolean iWalletUsed) {
        this.iWalletUsed = iWalletUsed;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
