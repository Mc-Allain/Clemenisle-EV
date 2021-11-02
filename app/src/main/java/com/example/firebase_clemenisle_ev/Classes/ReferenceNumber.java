package com.example.firebase_clemenisle_ev.Classes;

public class ReferenceNumber {

    String id, referenceNumber, timestamp;
    double value;

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
}
