package com.example.firebase_clemenisle_ev.Classes;

public class DateTimeDifference {
    String timestamp;
    int dayDifference = 0, monthDifference = 0, yearDifference = 0;

    public DateTimeDifference(String timestamp) {
        DateTimeToString dateTimeToString = new DateTimeToString();
        String dateNow = dateTimeToString.getDate();
        String timeNow = dateTimeToString.getTime(false);
        int minNow = Integer.parseInt(dateTimeToString.getMin());
        int hourNow = Integer.parseInt(dateTimeToString.getRawHour());
        int dayNow = Integer.parseInt(dateTimeToString.getDay());
        int monthNow = Integer.parseInt(dateTimeToString.getMonthNo());
        int yearNow = Integer.parseInt(dateTimeToString.getYear());

        dateTimeToString.setFormattedSchedule(timestamp);
        String iDateAndTime = dateTimeToString.getDateAndTime();
        String iDate = dateTimeToString.getDate();
        String iTime = dateTimeToString.getTime(false);
        int iMin = Integer.parseInt(dateTimeToString.getMin());
        int iHour = Integer.parseInt(dateTimeToString.getRawHour());
        int iDay = Integer.parseInt(dateTimeToString.getDay());
        int iMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int iYear = Integer.parseInt(dateTimeToString.getYear());
        int maxDay = dateTimeToString.getMaximumDaysInMonthOfYear();
        String iDateNoYear = iDay + " " + dateTimeToString.getMonth();

        if(dateNow.equals(iDate)) {
            if(timeNow.equals(iTime)) timestamp = "Just now";
            else {
                int hrDifference = hourNow - iHour;
                if(iHour > hourNow) hrDifference = hourNow + 24 - iHour;

                if(hrDifference == 0 || hrDifference == 1) {
                    int minDifference;
                    if(minNow > iMin)
                        minDifference = minNow - iMin;
                    else {
                        minDifference = minNow + 60 - iMin;
                        if(minDifference < 60) hrDifference--;
                    }

                    if(hrDifference == 1) timestamp = hrDifference + " hour ago";
                    else {
                        if(minDifference == 1) timestamp = minDifference + " minute ago";
                        else timestamp = minDifference + " minutes ago";
                    }
                }
                else if(hrDifference < 12) timestamp = hrDifference + " hours ago";
                else timestamp = iTime;
            }
        }
        else {
            yearDifference = yearNow - iYear;
            monthDifference = monthNow - iMonth;
            if(iMonth > monthNow) monthDifference = monthNow + 12 * yearDifference - iMonth;

            if(dayNow > iDay)
                dayDifference = dayNow - iDay;
            else dayDifference = dayNow + maxDay * monthDifference - iDay;

            if(dayDifference == 1) timestamp = "Yesterday | " + iTime;
            else if(dayDifference < 7) timestamp = dayDifference + " days ago | " + iTime;
            else if(monthDifference == 0) timestamp = iDateNoYear + " | " + iTime;
            else timestamp = iDateAndTime;
        }
        this.timestamp = timestamp;
    }

    public String getResult() {
        return timestamp;
    }

    public int getDayDifference() {
        return dayDifference;
    }

    public int getMonthDifference() {
        return monthDifference;
    }

    public int getYearDifference() {
        return yearDifference;
    }
}
