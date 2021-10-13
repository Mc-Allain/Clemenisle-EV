package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private BookingType bookingType;
    private Station endStation;
    private String id, message, schedule;
    private Station startStation;
    private String status;
    private final List<Route> routeList = new ArrayList<>();

    public Booking() {
    }

    public Booking(DataSnapshot dataSnapshot) {

        this.id = dataSnapshot.child("id").getValue(String.class);
        this.message = dataSnapshot.child("message").getValue(String.class);
        this.schedule = dataSnapshot.child("schedule").getValue(String.class);
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
    }

    public Booking(BookingType bookingType, Station endStation, String id, String message,
                    String schedule, Station startStation, String status) {
        this.bookingType = bookingType;
        this.endStation = endStation;
        this.id = id;
        this.message = message;
        this.schedule = schedule;
        this.startStation = startStation;
        this.status = status;
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

    public String getSchedule() {
        return schedule;
    }

    public String getFormattedSchedule() {
        DateTimeToString converter = new DateTimeToString(schedule);
        return converter.getDate() + " | " + converter.getTime();
    }

    public Station getStartStation() {
        return startStation;
    }

    public String getStatus() {
        return status;
    }

    public List<Route> getRouteList() {
        return routeList;
    }
}
