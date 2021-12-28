package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

public class Permission {

    boolean admin, bookingType, station, touristSpot, user;

    public Permission() {
    }

    public Permission(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("admin").exists())
            admin = dataSnapshot.child("admin").getValue(Boolean.class);
        if(dataSnapshot.child("bookingType").exists())
            bookingType = dataSnapshot.child("bookingType").getValue(Boolean.class);
        if(dataSnapshot.child("station").exists())
            station = dataSnapshot.child("station").getValue(Boolean.class);
        if(dataSnapshot.child("touristSpot").exists())
            touristSpot = dataSnapshot.child("touristSpot").getValue(Boolean.class);
        if(dataSnapshot.child("user").exists())
            user = dataSnapshot.child("user").getValue(Boolean.class);
    }

    public Permission(boolean admin, boolean bookingType, boolean station, boolean touristSpot, boolean user) {
        this.admin = admin;
        this.bookingType = bookingType;
        this.station = station;
        this.touristSpot = touristSpot;
        this.user = user;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isBookingType() {
        return bookingType;
    }

    public boolean isStation() {
        return station;
    }

    public boolean isTouristSpot() {
        return touristSpot;
    }

    public boolean isUser() {
        return user;
    }
}
