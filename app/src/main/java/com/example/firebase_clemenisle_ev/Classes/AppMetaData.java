package com.example.firebase_clemenisle_ev.Classes;

public class AppMetaData {

    String aboutApp;
    double currentVersion = 0.3, latestVersion = 0;
    String status;

    public AppMetaData() {
    }

    public String getAboutApp() {
        return aboutApp;
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
}
