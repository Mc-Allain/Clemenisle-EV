package com.example.firebase_clemenisle_ev.Classes;

public class IncomeTransaction {

    private String id, timestamp;
    private double value;

    public IncomeTransaction() {
    }

    public IncomeTransaction(String id, String timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }
}
