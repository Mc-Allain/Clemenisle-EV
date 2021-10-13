package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String emailAddress, firstName, id, lastName, middleName, password;
    List<SimpleTouristSpot> likedSpots = new ArrayList<>();
    List<Booking> bookingList = new ArrayList<>();

    public User() {
    }

    public User(DataSnapshot dataSnapshot) {
        emailAddress = dataSnapshot.child("emailAddress").getValue(String.class);
        firstName = dataSnapshot.child("firstName").getValue(String.class);
        id = dataSnapshot.child("id").getValue(String.class);
        lastName = dataSnapshot.child("lastName").getValue(String.class);
        middleName = dataSnapshot.child("middleName").getValue(String.class);
        password = dataSnapshot.child("password").getValue(String.class);

        likedSpots.clear();
        DataSnapshot likedSpotSnapshot = dataSnapshot.child("likedSpots");
        if(likedSpotSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : likedSpotSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    SimpleTouristSpot likedSpot = dataSnapshot1.getValue(SimpleTouristSpot.class);
                    if(!likedSpot.isDeactivated()) {
                        likedSpots.add(likedSpot);
                    }
                }
            }
        }

        bookingList.clear();
        DataSnapshot bookingSnapshot = dataSnapshot.child("bookingList");
        if(bookingSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : bookingSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Booking booking = new Booking(dataSnapshot1);
                    bookingList.add(booking);
                }
            }
        }
    }

    public User(String emailAddress, String firstName, String id, String lastName, String middleName, String password) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.id = id;
        this.lastName = lastName;
        this.middleName = middleName;
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getPassword() {
        return password;
    }

    public List<SimpleTouristSpot> getLikedSpots() {
        return likedSpots;
    }

    public List<Booking> getBookingList() {
        return bookingList;
    }
}
