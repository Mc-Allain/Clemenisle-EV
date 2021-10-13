package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DetailedTouristSpot {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    private int books;
    private boolean deactivated = false;
    private String description, id, img;
    private double lat = 14.590504700930238;
    private int likes;
    private double lng = 120.97456410527231;
    private String name;
    private int visits;
    private final List<SimpleTouristSpot> nearSpots = new ArrayList<>();
    private final List<Station> nearStations = new ArrayList<>();

    public DetailedTouristSpot() {
    }

    public DetailedTouristSpot(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("deactivated").exists())
            deactivated = dataSnapshot.child("deactivated").getValue(boolean.class);
        description = dataSnapshot.child("description").getValue(String.class);
        id = dataSnapshot.child("id").getValue(String.class);
        img = dataSnapshot.child("img").getValue(String.class);
        if(dataSnapshot.child("lat").exists())
            lat = dataSnapshot.child("lat").getValue(double.class);
        if(dataSnapshot.child("lng").exists())
            lng = dataSnapshot.child("lng").getValue(double.class);
        this.name = dataSnapshot.child("name").getValue(String.class);

        nearSpots.clear();
        DataSnapshot spotSnapshot = dataSnapshot.child("nearSpots");
        if(spotSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : spotSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    SimpleTouristSpot nearSpot = dataSnapshot1.getValue(SimpleTouristSpot.class);
                    if(!nearSpot.isDeactivated()) {
                        nearSpots.add(nearSpot);
                    }
                }
            }
        }

        nearStations.clear();
        DataSnapshot stationSnapshot = dataSnapshot.child("nearStations");
        if(stationSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : stationSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Station nearStation = dataSnapshot1.getValue(Station.class);
                    if(!nearStation.isDeactivated()) {
                        nearStations.add(nearStation);
                    }
                }
            }
        }
    }

    public DetailedTouristSpot(String description, String id, String img, double lat, double lng,
                               String name) {
        this.description = description;
        this.id = id;
        this.img = img;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public int getBooks() {
        return books;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public double getLat() {
        return lat;
    }

    public int getLikes() {
        return likes;
    }

    public double getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }

    public int getVisits() {
        return visits;
    }

    public List<SimpleTouristSpot> getNearSpots() {
        return nearSpots;
    }

    public List<Station> getNearStations() {
        return nearStations;
    }

    public void setBooks(int books) {
        this.books = books;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }
}
