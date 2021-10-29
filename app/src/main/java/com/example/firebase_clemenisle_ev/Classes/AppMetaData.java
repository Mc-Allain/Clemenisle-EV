package com.example.firebase_clemenisle_ev.Classes;

public class AppMetaData {

    String aboutApp,
            newlyAddedFeatures = "● Notification Added\n" +
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
    double currentVersion = 0.3, latestVersion = 0;
    String status;
    boolean isDeveloper = false;

    public AppMetaData() {
    }

    public String getAboutApp() {
        return aboutApp;
    }

    public String getNewlyAddedFeatures() {
        return newlyAddedFeatures;
    }

    public double getCurrentVersion() {
        return currentVersion;
    }

    public double getLatestVersion() {
        return latestVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setAboutApp(String aboutApp) {
        this.aboutApp = aboutApp;
    }

    public void setLatestVersion(double latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeveloper() {
        return isDeveloper;
    }
}
