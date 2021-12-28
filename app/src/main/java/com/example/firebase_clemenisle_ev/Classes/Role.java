package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

public class Role {

    private boolean developer, admin, driver, owner;

    public Role() {
    }

    public Role(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("developer").exists())
            developer = dataSnapshot.child("developer").getValue(Boolean.class);
        if(dataSnapshot.child("admin").exists())
            admin = dataSnapshot.child("admin").getValue(Boolean.class);
        if(dataSnapshot.child("driver").exists())
            driver = dataSnapshot.child("driver").getValue(Boolean.class);
        if(dataSnapshot.child("owner").exists())
            owner = dataSnapshot.child("owner").getValue(Boolean.class);
    }

    public Role(boolean developer, boolean admin, boolean driver, boolean owner) {
        this.developer = developer;
        this.admin = admin;
        this.driver = driver;
        this.owner = owner;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isDriver() {
        return driver;
    }

    public boolean isOwner() {
        return owner;
    }
}
