package com.example.firebase_clemenisle_ev.Classes;

public class OnTheSpotBooking {

    BookingType bookingType;
    SimpleTouristSpot destinationSpot;
    double originLat, originLng;
    String id, message, schedule, status;

    public OnTheSpotBooking() {
    }

    public OnTheSpotBooking(BookingType bookingType, DetailedTouristSpot destinationSpot,
                            double originLat, double originLng, String id, String message,
                            String schedule, String status) {
        this.bookingType = bookingType;
        this.destinationSpot = new SimpleTouristSpot(false, destinationSpot.getId(),
                destinationSpot.getImg(), destinationSpot.getName());
        this.originLat = originLat;
        this.originLng = originLng;
        this.id = id;
        this.message = message;
        this.schedule = schedule;
        this.status = status;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public SimpleTouristSpot getDestinationSpot() {
        return destinationSpot;
    }

    public double getOriginLat() {
        return originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getStatus() {
        return status;
    }
}
