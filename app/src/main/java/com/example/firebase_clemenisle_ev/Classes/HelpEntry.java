package com.example.firebase_clemenisle_ev.Classes;

public class HelpEntry {

    String timestamp = new DateTimeToString().getDateAndTime(), value;

    public HelpEntry() {
    }

    public HelpEntry(String value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }
}
