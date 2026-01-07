package com.covoit.covoiturage.model;

/**
 * Petit objet de vue pour la page "Demandes reçues" (conducteur).
 * Il regroupe:
 *  - la réservation (booking)
 *  - le trajet (ride)
 *  - le passager qui a fait la demande (passenger)
 */

public class DriverBookingView {

    private Booking booking;
    private Ride ride;
    private User passenger;

    public DriverBookingView() {
    }

    public DriverBookingView(Booking booking, Ride ride, User passenger) {
        this.booking = booking;
        this.ride = ride;
        this.passenger = passenger;
    }

    // ------------- Getters et setters   ----------------------
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

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }
}
