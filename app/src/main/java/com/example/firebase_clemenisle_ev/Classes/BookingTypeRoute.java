package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingTypeRoute {

    private Station endStation;
    private String id;
    private Station startStation;
    private final List<SimpleTouristSpot> spots = new ArrayList<>();

    public BookingTypeRoute() {
    }

    public BookingTypeRoute(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("endStation").exists())
            this.endStation = dataSnapshot.child("endStation").getValue(Station.class);
        this.id = dataSnapshot.child("id").getValue(String.class);
        if(dataSnapshot.child("startStation").exists())
            this.startStation = dataSnapshot.child("startStation").getValue(Station.class);

        spots.clear();
        DataSnapshot spotSnapshot = dataSnapshot.child("routeSpots");
        if(spotSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : spotSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    SimpleTouristSpot spot = new SimpleTouristSpot(dataSnapshot1);
                    if(!spot.isDeactivated()) {
                        spots.add(spot);
                    }
                }
            }
        }
    }

    public BookingTypeRoute(String id) {
        this.id = id;
    }

    public Station getEndStation() {
        return endStation;
    }

    public String getId() {
        return id;
    }

    public List<SimpleTouristSpot> getSpots() {
        return spots;
    }

    public Station getStartStation() {
        return startStation;
    }
}
