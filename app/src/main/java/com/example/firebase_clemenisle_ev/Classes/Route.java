package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

public class Route {
    private boolean deactivated = false;
    private String id, img, name, routeId;
    private boolean visited = false;
    private int books, visits;

    public Route() {
    }

    public Route(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("deactivated").exists())
            this.deactivated = dataSnapshot.child("deactivated").getValue(boolean.class);
        this.id = dataSnapshot.child("id").getValue(String.class);
        this.img = dataSnapshot.child("img").getValue(String.class);
        this.name = dataSnapshot.child("name").getValue(String.class);
        this.routeId = dataSnapshot.child("routeId").getValue(String.class);
        if(dataSnapshot.child("visited").exists())
            this.deactivated = dataSnapshot.child("visited").getValue(boolean.class);
    }

    public Route(boolean deactivated, String id, String img, String name, String routeId, boolean visited) {
        this.deactivated = deactivated;
        this.id = id;
        this.img = img;
        this.name = name;
        this.routeId = routeId;
        this.visited = visited;
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

    public String getRouteId() {
        return routeId;
    }

    public boolean isVisited() {
        return visited;
    }

    public int getBooks() {
        return books;
    }

    public void setBooks(int books) {
        this.books = books;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }
}
