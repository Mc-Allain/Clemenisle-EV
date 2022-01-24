package com.example.firebase_clemenisle_ev.Classes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateTimeToString {

    private boolean isDefaultDate = true;

    private String[] dateSplit;
    private String[] timeSplit;

    String[] formattedSchedule;
    String formattedDate;
    private String formattedMonth = null;

    private int _12HrFormat;
    private String timeMode = null;

    public DateTimeToString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd H:mm:00",
                Locale.getDefault());
        String currentDateAndTime = simpleDateFormat.format(new Date());
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
        isDefaultDate = false;
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
        if(dateSplit.length == 3) {
            int rawMonth = Integer.parseInt(dateSplit[1]);
            if(isDefaultDate) rawMonth--;

            String month = String.valueOf(rawMonth);
            if(month.length() == 1) month = "0" + month;

            return month;
        }
        return "Invalid Date";
    }

    public String getMonth() {
        if(dateSplit.length == 3) {
            String month;

            int rawMonth = Integer.parseInt(dateSplit[1]);
            if(isDefaultDate) rawMonth--;

            switch (rawMonth) {
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

    public String getDayOfWeek() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(getYear()), Integer.parseInt(getMonthNo()), Integer.parseInt(getDay()));

        return simpleDateFormat.format(calendar.getTime());
    }

    public int getMaximumDaysInMonthOfYear() {
        if(dateSplit.length == 3) {
            int rawMonth = Integer.parseInt(dateSplit[1]);
            if(isDefaultDate) rawMonth--;

            List<Integer> _31DaysMonth = Arrays.asList(0, 2, 4, 6, 7, 9, 11);
            List<Integer> _30DaysMonth = Arrays.asList(3, 5, 8, 10);
            int _28DaysMonth = 1;

            if(_31DaysMonth.contains(rawMonth))
                return 31;
            if(_30DaysMonth.contains(rawMonth))
                return 30;
            if(_28DaysMonth == rawMonth)
                return Integer.parseInt(getYear()) % 4 == 0 ? 29 : 28;

        }
        return 28;
    }

    public String getDate() {
        if(dateSplit.length == 3) return getDay() + " " + getMonth() + " " + getYear();
        return "Invalid Date";
    }

    public String getDateNo(boolean reverse) {
        if(dateSplit.length == 3) {
            if(reverse) return getYear() + " " + getMonthNo() + " " + getDay();
            return getDay() + " " + getMonthNo() + " " + getYear();
        };
        return "Invalid Date";
    }

    private String formattedHour(int index) {
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
            if(index == 0) return String.valueOf(hour);
            else if(index == 1) return time;
        }
        return "Invalid Time";
    }

    public String getRawHour() {
        return timeSplit[0];
    }

    public String getHour() {
        return formattedHour(0);
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
        if(timeSplit.length == 3) return formattedHour(1);
        return "Invalid Time";
    }

    public String getTime(boolean _24HourFormat) {
        if(timeSplit.length == 3) {
            if(_24HourFormat) {
                String hour = getRawHour();
                if(hour.length() == 1) hour = "0" + hour;
                return hour + ":" + getMin();
            }
            return  getHour() + ":" + getMin() + " " + getTimeMode();
        }
        return "Invalid Time";
    }

    public String getDateAndTime() {
        return getDate() + " | " + getTime(false);
    }

    public String getNoDateAndTime() {
        return getDateNo(false) + " | " + getTime(true);
    }

    public void setFormattedSchedule(String formattedSchedule) {
        this.formattedSchedule = formattedSchedule.split("\\|");
        this.formattedDate = this.formattedSchedule[0].trim();
        this.formattedMonth = formattedDate.split(" ")[1];

        String year = formattedDate.split(" ")[2];
        String day = formattedDate.split(" ")[0];

        this.setDateToSplit(year + "-" + getMonthNoFromFormattedMonth() + "-" + day);

        if(this.formattedSchedule.length > 0) {
            String formattedTime = this.formattedSchedule[1].trim();
            this._12HrFormat = Integer.parseInt(formattedTime.split(":")[0]);
            this.timeMode = formattedTime.split(" ")[1];

            String minute = formattedTime.split(":")[1];
            
            this.setTimeToSplit(
                    getRawHourFromFormattedHour() + ":" +
                            minute.split(" ")[0] + ":00"
            );
        }
    }

    private String getRawHourFromFormattedHour() {
        if(_12HrFormat > 0) {
            if(_12HrFormat == 12 && timeMode.equals("AM"))
                _12HrFormat = 0;
            if(_12HrFormat != 12 && timeMode.equals("PM"))
                _12HrFormat += 12;
            return String.valueOf(_12HrFormat);
        }
        return "Invalid Time";
    }

    private String getMonthNoFromFormattedMonth() {
        if(formattedMonth != null) {
            String month;

            switch (formattedMonth) {
                case "February":
                    month = "01";
                    break;
                case "March":
                    month = "02";
                    break;
                case "April":
                    month = "03";
                    break;
                case "May":
                    month = "04";
                    break;
                case "June":
                    month = "05";
                    break;
                case "July":
                    month = "06";
                    break;
                case "August":
                    month = "07";
                    break;
                case "September":
                    month = "08";
                    break;
                case "October":
                    month = "09";
                    break;
                case "November":
                    month = "10";
                    break;
                case "December":
                    month = "11";
                    break;
                default:
                    month = "00";
            }
            return month;
        }
        return "Invalid Date";
    }
}
