package com.example.firebase_clemenisle_ev.Classes;

public class DateTimeDifference {
    String timestamp;

    public DateTimeDifference(String timestamp) {
        DateTimeToString dateTimeToString = new DateTimeToString();
        String dateNow = dateTimeToString.getDate();
        String timeNow = dateTimeToString.getTime();
        int minNow = Integer.parseInt(dateTimeToString.getMin());
        int hourNow = Integer.parseInt(dateTimeToString.getRawHour());
        int dayNow = Integer.parseInt(dateTimeToString.getDay());
        int monthNow = Integer.parseInt(dateTimeToString.getMonthNo());
        int yearNow = Integer.parseInt(dateTimeToString.getYear());

        dateTimeToString.setFormattedSchedule(timestamp);
        String chatDate = dateTimeToString.getDate();
        String chatTime = dateTimeToString.getTime();
        int chatMin = Integer.parseInt(dateTimeToString.getMin());
        int chatHour = Integer.parseInt(dateTimeToString.getRawHour());
        int chatDay = Integer.parseInt(dateTimeToString.getDay());
        int chatMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int chatYear = Integer.parseInt(dateTimeToString.getYear());
        int chatMaxDay = dateTimeToString.getMaximumDaysInMonthOfYear();
        String chatDateNoYear = chatDay + " " + dateTimeToString.getMonth();

        if(dateNow.equals(chatDate)) {
            if(timeNow.equals(chatTime)) timestamp = "Just now";
            else {
                int hrDifference = hourNow - chatHour;
                if(hrDifference == 0 || hrDifference == 1) {
                    int minDifference;
                    if(minNow > chatMin)
                        minDifference = minNow - chatMin;
                    else {
                        minDifference = minNow + 60 - chatMin;
                        if(minDifference < 60) hrDifference--;
                    }

                    if(hrDifference == 1) timestamp = hrDifference + " hour ago";
                    else {
                        if(minDifference == 1) timestamp = minDifference + " minute ago";
                        else timestamp = minDifference + " minutes ago";
                    }
                }
                else if(hrDifference < 12) timestamp = hrDifference + " hours ago";
                else timestamp = chatTime;
            }
        }
        else {
            int yearDifference = yearNow - chatYear;
            int monthDifference = monthNow - chatMonth;

            if(monthDifference == 0 || monthDifference == 1 && yearDifference == 0) {
                int dayDifference;
                if(dayNow > chatDay)
                    dayDifference = dayNow - chatDay;
                else dayDifference = dayNow + chatMaxDay - chatDay;

                if(dayDifference == 1) timestamp = "Yesterday";
                else if(dayDifference < 7) timestamp = dayDifference + " days ago";
                else timestamp = chatDateNoYear;
            }
            else timestamp = chatDate;
        }
        this.timestamp = timestamp;
    }

    public String getResult() {
        return timestamp;
    }
}