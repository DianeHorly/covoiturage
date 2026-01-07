package com.covoit.covoiturage.model;


/**
 * Petit objet de vue pour la page "Mes réservations".
 * Associe une réservation et le trajet correspondant.
 */
public class BookingWithRide {

    private Booking booking;
    private Ride ride;

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }
}
