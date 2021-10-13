package com.example.firebase_clemenisle_ev.Classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeToString {

    private final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd H:mm:00", Locale.getDefault());

    String currentDateAndTime = simpleDateFormat.format(new Date());

    private String[] dateSplit;
    private String[] timeSplit;

    public DateTimeToString() {
        String[] scheduleSplit = currentDateAndTime.split(" ");
        this.dateSplit = scheduleSplit[0].split("-");
        this.timeSplit = scheduleSplit[1].split(":");
    }

    public DateTimeToString(String schedule) {
        String[] scheduleSplit = schedule.split(" ");
        this.dateSplit = scheduleSplit[0].split("-");
        this.timeSplit = scheduleSplit[1].split(":");
    }

    public void setDateToSplit(String value) {
        this.dateSplit = value.split("-");
    }

    public void setTimeToSplit(String value) {
        this.timeSplit = value.split(":");
    }

    public String getYear2Suffix() {
        if(dateSplit.length == 3) return dateSplit[0].substring(2,4);
        return "Invalid Date";
    }

    public String getYear() {
        if(dateSplit.length == 3) return dateSplit[0];
        return "Invalid Date";
    }

    public String getMonthNo() {
        if(dateSplit.length == 3) return dateSplit[1];
        return "Invalid Date";
    }

    public String getMonth() {
        if(dateSplit.length == 3) {
            String month;

            switch (Integer.parseInt(dateSplit[1])) {
                case 2:
                    month = "February";
                    break;
                case 3:
                    month = "March";
                    break;
                case 4:
                    month = "April";
                    break;
                case 5:
                    month = "May";
                    break;
                case 6:
                    month = "June";
                    break;
                case 7:
                    month = "July";
                    break;
                case 8:
                    month = "August";
                    break;
                case 9:
                    month = "September";
                    break;
                case 10:
                    month = "October";
                    break;
                case 11:
                    month = "November";
                    break;
                case 12:
                    month = "December";
                    break;
                default:
                    month = "January";
            }
            return month;
        }
        return "Invalid Date";
    }

    public String getDay() {
        if(dateSplit.length == 3) return dateSplit[2];
        return "Invalid Date";
    }

    public String getDate() {
        if(dateSplit.length == 3) return getDay() + " " + getMonth() + " " + getYear();
        return "Invalid Date";
    }

    private String processHour(int mode) {
        if(timeSplit.length == 3) {
            int hour = Integer.parseInt(timeSplit[0]);
            String time = "AM";

            if(hour >= 12) {
                time = "PM";
                if(hour > 12) {
                    hour -= 12;
                }
            }
            else {
                if(hour == 0) {
                    hour = 12;
                }
            }
            if(mode == 0) return String.valueOf(hour);
            else if(mode == 1) return time;
        }
        return "Invalid Time";
    }

    public String getHour() {
        return processHour(0);
    }

    public String getMin() {
        if(timeSplit.length == 3) return timeSplit[1];
        return "Invalid Time";
    }

    public String getSec() {
        if(timeSplit.length == 3) return timeSplit[2];
        return "Invalid Time";
    }

    public String getTimeMode() {
        if(timeSplit.length == 3) return processHour(1);
        return "Invalid Time";
    }

    public String getTime() {
        if(timeSplit.length == 3) return  getHour() + ":" + getMin() + " " + getTimeMode();
        return "Invalid Time";
    }

    public String getDateAndTime() {
        return getDate() + " " + getTime();
    }
}
