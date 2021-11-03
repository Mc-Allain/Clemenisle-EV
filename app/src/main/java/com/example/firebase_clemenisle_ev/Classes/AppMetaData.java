package com.example.firebase_clemenisle_ev.Classes;

public class AppMetaData {

    String newlyAddedFeatures = "● Chat List Added\n" +
            "● Online Payment Added";
    double currentVersion = 0.31;
    boolean isDeveloper = false;

    public AppMetaData() {
    }

    public String getNewlyAddedFeatures() {
        return newlyAddedFeatures;
    }

    public double getCurrentVersion() {
        return currentVersion;
    }

    public boolean isDeveloper() {
        return isDeveloper;
    }
}
