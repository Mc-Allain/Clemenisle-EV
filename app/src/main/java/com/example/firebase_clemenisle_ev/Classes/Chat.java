package com.example.firebase_clemenisle_ev.Classes;

public class Chat {

    String id, senderId, message, timestamp;
    String endPointUserId, driverUserId, status;
    Booking booking;

    public Chat() {
    }

    public Chat(String id, String senderId, String message, String timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getEndPointUserId() {
        return endPointUserId;
    }

    public void setEndPointUserId(String endPointUserId) {
        this.endPointUserId = endPointUserId;
    }

    public String getDriverUserId() {
        return driverUserId;
    }

    public void setDriverUserId(String driverUserId) {
        this.driverUserId = driverUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
