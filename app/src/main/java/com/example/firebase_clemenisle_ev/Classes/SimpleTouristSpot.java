package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

public class SimpleTouristSpot {

    private boolean deactivated = false;
    private String id;
    private String img;
    private String name;

    private double lat, lng;

    boolean unliked = true;

    public SimpleTouristSpot() {
    }

    public SimpleTouristSpot(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("deactivated").exists())
            this.deactivated = dataSnapshot.child("deactivated").getValue(boolean.class);
        this.id = dataSnapshot.child("id").getValue(String.class);
        this.img = dataSnapshot.child("img").getValue(String.class);
        this.name = dataSnapshot.child("name").getValue(String.class);
    }

    public SimpleTouristSpot(DetailedTouristSpot touristSpot) {
        this.deactivated = touristSpot.isDeactivated();
        this.id = touristSpot.getId();
        this.img = touristSpot.getImg();
        this.name = touristSpot.getName();
        this.lat = touristSpot.getLat();
        this.lng = touristSpot.getLng();
    }

    public SimpleTouristSpot(boolean deactivated, String id, String img, String name) {
        this.deactivated = deactivated;
        this.id = id;
        this.img = img;
        this.name = name;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isUnliked() {
        return unliked;
    }

    public void setUnliked(boolean unliked) {
        this.unliked = unliked;
    }
}
