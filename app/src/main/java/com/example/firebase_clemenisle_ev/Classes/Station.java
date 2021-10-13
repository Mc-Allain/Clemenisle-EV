package com.example.firebase_clemenisle_ev.Classes;

public class Station {
    private boolean deactivated;
    private String id;
    private double lat = 14.590504700930238, lng = 20.97456410527231;
    private String name;

    public Station() {
    }

    public Station(boolean deactivated, String id, double lat, double lng, String name) {
        this.deactivated = deactivated;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }
}
