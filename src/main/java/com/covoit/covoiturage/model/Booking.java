package com.covoit.covoiturage.model;

import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Représente une réservation (booking) d'un trajet.
 */
public class Booking {

    private ObjectId id;           // id de la réservation
    private ObjectId rideId;       // id du trajet réservé
    private ObjectId passengerId;  // id de l'utilisateur passager
    private int seats;             // nombre de places réservées
    private LocalDateTime createdAt; // date de création de la réservation

    private String ticketCode; // code unique du ticket utilisé pour le QR Code
    private String status;     // "CONFIRMED", "PENDING", "CANCELLED", "REJECTED"
    private String driverMessage; // message de confirmation ou refus du conducteur
    
    
    // Sous-trajet réservé (pour l'affichage)
    private String fromCity;   
    private String toCity;      

    // Indices du sous-trajet dans ride.getFullPath()
    private Integer fromIndex;   // peut être null
    private Integer toIndex;     // peut être null
    // Tarifs
    private int pricePerSeat;   // prix / place pour CE sous-trajet
    private int totalPrice;     // prix total de cette réservation
    
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    
    // ---- -----  setters et getters -------------- 
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getRideId() {
        return rideId;
    }

    public void setRideId(ObjectId rideId) {
        this.rideId = rideId;
    }

    public ObjectId getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(ObjectId passengerId) {
        this.passengerId = passengerId;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // status du ticket
    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // confirmationou refus du conducteur
    public String getDriverMessage() { 
    	return driverMessage; 
    	}
    
    public void setDriverMessage(String driverMessage) {
    	this.driverMessage = driverMessage; 
    }
    
    
    // ----------------- Helpers d'affichage -----------------

    public String getCreatedAtFormatted() {
        if (createdAt == null) 
        	return "";
        return createdAt.format(DISPLAY_FMT);
    }
    
    public String getStatusLabel() {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "PENDING":   return "En attente";
            case "CONFIRMED": return "Confirmée";
            case "REJECTED":  return "Refusée";
            case "CANCELLED": return "Annulée";
            default:  return status;
        }
    }
    
    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary";

        return switch (status.toUpperCase()) {
            case "PENDING"   -> "bg-warning text-dark";
            case "CONFIRMED" -> "bg-success";
            case "REJECTED"  -> "bg-danger";
            case "CANCELLED" -> "bg-secondary";
            default          -> "bg-secondary";
        };
    }
    
    /* Si fromCity/toCity sont vides-> afficher le trajet complet.
    */
   public String getSegmentDisplay() {
       if (fromCity != null && toCity != null && !fromCity.isBlank() && !toCity.isBlank()) {
           return fromCity + " \u2192 " + toCity;  // ->
       }
       
       return "";
   }
    
    // -------    Getters et setters pour la gestion des sous trajets  ---------------------
    public String getFromCity() { 
    	return fromCity; 
    }
    public void setFromCity(String fromCity) { 
    	this.fromCity = fromCity; 
    }

    public String getToCity() { return toCity; }
    public void setToCity(String toCity) { this.toCity = toCity; }

    public int getFromIndex() { return fromIndex; }
    public void setFromIndex(int fromIndex) { this.fromIndex = fromIndex; }

    public int getToIndex() { return toIndex; }
    public void setToIndex(int toIndex) { this.toIndex = toIndex; }

    public int getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(int pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    /* Pour "Mes réservations"
    public String getSegmentLabel() {
        if (fromCity == null || toCity == null) return "";
        return fromCity + " \u2192 " + toCity;     // ->
    }
*/
    
}
