package com.example.firebase_clemenisle_ev.Classes;

public class ScheduleTime {

    boolean deactivated = false;
    String id, time;

    public ScheduleTime() {
    }

    public ScheduleTime(boolean deactivated, String id, String time) {
        this.deactivated = deactivated;
        this.id = id;
        this.time = time;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }
}
