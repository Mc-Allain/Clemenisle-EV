package com.example.firebase_clemenisle_ev.Classes;

public class ReferenceNumber {

    String referenceNumber, timestamp;
    double value;

    public ReferenceNumber(String referenceNumber, String timestamp, double value) {
        this.referenceNumber = referenceNumber;
        this.timestamp = timestamp;
        this.value = value;
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
}
