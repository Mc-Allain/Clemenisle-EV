package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private BookingType bookingType;
    private Station endStation;
    private String id, message, schedule, timestamp;
    private boolean paid;
    private Station startStation;
    private String status;
    private final List<Route> routeList = new ArrayList<>();

    private SimpleTouristSpot destinationSpot;
    private double originLat, originLng;

    boolean notified = true, read = false;
    private final List<Chat> chats = new ArrayList<>();
    private String previousDriverUserId;

    List<ReferenceNumber> referenceNumberList = new ArrayList<>();
    double refundedAmount;

    String pickUpTime, dropOffTime;

    String reason;

    public Booking() {
    }

    public Booking(Booking booking) {
        this.bookingType = booking.getBookingType();
        this.endStation = booking.getEndStation();
        this.id = booking.getId();
        this.message = booking.getMessage();
        this.schedule = booking.getSchedule();
        this.timestamp = booking.getTimestamp();
        this.paid = booking.isPaid();
        this.startStation = booking.getStartStation();
        this.status = booking.getStatus();

        this.destinationSpot = booking.getDestinationSpot();
        if(startStation == null) {
            this.originLat = booking.getOriginLat();
            this.originLng = booking.getOriginLng();
        }
        else {
            this.originLat = startStation.getLat();
            this.originLng = startStation.getLng();
        }
    }

    public Booking(BookingType bookingType, DetailedTouristSpot destinationSpot,
                   double originLat, double originLng, String id, String message,
                   String schedule, String timestamp, String status) {
        this.bookingType = bookingType;
        this.destinationSpot = new SimpleTouristSpot(destinationSpot);
        this.originLat = originLat;
        this.originLng = originLng;
        this.id = id;
        this.message = message;
        this.schedule = schedule;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Booking(DataSnapshot dataSnapshot) {
        this.id = dataSnapshot.child("id").getValue(String.class);
        this.message = dataSnapshot.child("message").getValue(String.class);
        this.schedule = dataSnapshot.child("schedule").getValue(String.class);
        this.timestamp = dataSnapshot.child("timestamp").getValue(String.class);

        if(dataSnapshot.child("paid").exists())
            this.paid = dataSnapshot.child("paid").getValue(Boolean.class);
        if(dataSnapshot.child("startStation").exists())
            this.startStation = dataSnapshot.child("startStation").getValue(Station.class);
        if(dataSnapshot.child("endStation").exists())
            this.endStation = dataSnapshot.child("endStation").getValue(Station.class);
        this.status = dataSnapshot.child("status").getValue(String.class);
        this.bookingType = new BookingType(dataSnapshot.child("bookingType"));

        routeList.clear();
        DataSnapshot routeSnapshot = dataSnapshot.child("routeSpots");
        if(routeSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : routeSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Route route = dataSnapshot1.getValue(Route.class);
                    if(!route.isDeactivated()) {
                        routeList.add(route);
                    }
                }
            }
        }

        if(dataSnapshot.child("destinationSpot").exists())
            this.destinationSpot = dataSnapshot.child("destinationSpot").getValue(SimpleTouristSpot.class);
        if(dataSnapshot.child("originLat").exists())
            this.originLat = dataSnapshot.child("originLat").getValue(Double.class);
        if(dataSnapshot.child("originLng").exists())
            this.originLng = dataSnapshot.child("originLng").getValue(Double.class);

        chats.clear();
        DataSnapshot chatSnapshot = dataSnapshot.child("chats");
        if(chatSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : chatSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Chat chat = dataSnapshot1.getValue(Chat.class);
                    chats.add(chat);
                }
            }
        }

        if(dataSnapshot.child("notified").exists())
            this.notified = dataSnapshot.child("notified").getValue(Boolean.class);
        if(dataSnapshot.child("read").exists())
            this.read = dataSnapshot.child("read").getValue(Boolean.class);
        this.previousDriverUserId = dataSnapshot.child("previousDriverUserId").getValue(String.class);

        referenceNumberList.clear();
        DataSnapshot referenceNumberSnapshot = dataSnapshot.child("referenceNumberList");
        if(referenceNumberSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : referenceNumberSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    ReferenceNumber referenceNumber = dataSnapshot1.getValue(ReferenceNumber.class);
                    referenceNumberList.add(referenceNumber);
                }
            }
        }
        if(dataSnapshot.child("refundedAmount").exists())
            this.refundedAmount = dataSnapshot.child("refundedAmount").getValue(Double.class);

        this.pickUpTime = dataSnapshot.child("pickUpTime").getValue(String.class);
        this.dropOffTime = dataSnapshot.child("dropOffTime").getValue(String.class);

        this.reason = dataSnapshot.child("reason").getValue(String.class);
    }

    public Booking(BookingType bookingType, Station endStation, String id, String message,
                   String schedule, String timestamp, boolean paid, Station startStation, String status) {
        this.bookingType = bookingType;
        this.endStation = endStation;
        this.id = id;
        this.message = message;
        this.schedule = schedule;
        this.timestamp = timestamp;
        this.paid = paid;
        this.startStation = startStation;
        this.status = status;
        this.originLat = startStation.getLat();
        this.originLng = startStation.getLng();
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public Station getEndStation() {
        return endStation;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Station getStartStation() {
        return startStation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Route> getRouteList() {
        return routeList;
    }

    public SimpleTouristSpot getDestinationSpot() {
        return destinationSpot;
    }

    public double getOriginLat() {
        return originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public boolean isNotified() {
        return notified;
    }

    public boolean isRead() {
        return read;
    }

    public String getPreviousDriverUserId() {
        return previousDriverUserId;
    }

    public void setPreviousDriverUserId(String previousDriverUserId) {
        this.previousDriverUserId = previousDriverUserId;
    }

    public List<ReferenceNumber> getReferenceNumberList() {
        return referenceNumberList;
    }

    public double getRefundedAmount() {
        return refundedAmount;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public String getDropOffTime() {
        return dropOffTime;
    }

    public String getReason() {
        return reason;
    }
}
