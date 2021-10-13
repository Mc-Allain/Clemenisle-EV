package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingType {
    private boolean deactivated = false;
    private String id;
    private String name;
    private double price;
    private List<BookingTypeRoute> routeList = new ArrayList<>();

    public BookingType() {
    }

    public BookingType(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("deactivated").exists())
            this.deactivated = dataSnapshot.child("deactivated").getValue(boolean.class);
        this.id = dataSnapshot.child("id").getValue(String.class);
        this.name = dataSnapshot.child("name").getValue(String.class);
        if(dataSnapshot.child("price").exists())
            this.price = dataSnapshot.child("price").getValue(double.class);

        routeList.clear();
        DataSnapshot spotSnapshot = dataSnapshot.child("routeList");
        if(spotSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : spotSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    BookingTypeRoute route = new BookingTypeRoute(dataSnapshot1);
                    routeList.add(route);
                }
            }
        }
    }

    public BookingType(boolean deactivated, String id, String name, double price) {
        this.deactivated = deactivated;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public List<BookingTypeRoute> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<BookingTypeRoute> routeList) {
        this.routeList = routeList;
    }
}
