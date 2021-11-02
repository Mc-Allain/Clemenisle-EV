package com.example.firebase_clemenisle_ev.Classes;

public class AppMetaData {

    String newlyAddedFeatures = "● Notification Added\n" +
                    "● Comment System Added\n" +
                    "● Application Status Alert Added\n" +
                    "● Application Update Alert Added\n" +
                    "● Update Notes Added\n" +
                    "● Profile Image Added\n" +
                    "● Booking Alert Dialog Added\n" +
                    "● Help Section Added\n" +
                    "● Preference Section Added\n" +
                    "● On The Spot(Single Spot) Service Added\n" +
                    "● Driver's Module Added\n" +
                    "\t○ Task System Added\n" +
                    "\t○ Task Request Function Added\n" +
                    "\t○ Booking Chat Added\n" +
                    "\t○ Chat Notification Added\n" +
                    "● Booking Record's QR Code Added";
    double currentVersion = 0.3;
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
