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

    String[] formattedSchedule;
    String formattedDate;
    private String formattedMonth = null;

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
                case 1:
                    month = "February";
                    break;
                case 2:
                    month = "March";
                    break;
                case 3:
                    month = "April";
                    break;
                case 4:
                    month = "May";
                    break;
                case 5:
                    month = "June";
                    break;
                case 6:
                    month = "July";
                    break;
                case 7:
                    month = "August";
                    break;
                case 8:
                    month = "September";
                    break;
                case 9:
                    month = "October";
                    break;
                case 10:
                    month = "November";
                    break;
                case 11:
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

    public String getDateNo() {
        if(dateSplit.length == 3) return getDay() + " " + getMonthNo() + " " + getYear();
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

    public void setFormattedSchedule(String formattedSchedule) {
        this.formattedSchedule = formattedSchedule.split("\\|");
        this.formattedDate = this.formattedSchedule[0].trim();
        this.formattedMonth = formattedDate.split(" ")[1];

        String year = formattedDate.split(" ")[2];
        String day = formattedDate.split(" ")[0];

        this.setDateToSplit(year + "-" + getMonthNoFromFormattedMonth() + "-" + day);
    }

    private String getMonthNoFromFormattedMonth() {
        if(formattedMonth != null) {
            int month;

            switch (formattedMonth) {
                case "February":
                    month = 1;
                    break;
                case "March":
                    month = 2;
                    break;
                case "April":
                    month = 3;
                    break;
                case "May":
                    month = 4;
                    break;
                case "June":
                    month = 5;
                    break;
                case "July":
                    month = 6;
                    break;
                case "August":
                    month = 7;
                    break;
                case "September":
                    month = 8;
                    break;
                case "October":
                    month = 9;
                    break;
                case "November":
                    month = 10;
                    break;
                case "December":
                    month = 11;
                    break;
                default:
                    month = 0;
            }
            return String.valueOf(month);
        }
        return "Invalid Date";
    }
}
