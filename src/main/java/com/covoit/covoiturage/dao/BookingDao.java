package com.covoit.covoiturage.dao;

import com.covoit.covoiturage.config.MongoManager;
import com.covoit.covoiturage.model.Booking;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import com.covoit.covoiturage.util.DateTimeUtils;


/**
 * DAO pour la collection "bookings" (réservations). permet de:
 * - insérer une réservation,
 * - récupérer les réservations d’un passager,
 * - calculer le nombre total de places déjà réservées pour un trajet (pour vérifier la dispo)
 */
public class BookingDao {

    private final MongoCollection<Document> col;
    //private final ZoneId zoneId = ZoneId.systemDefault();

    public BookingDao() {
        MongoDatabase db = MongoManager.getDatabase();
        this.col = db.getCollection("bookings");
    }

    /**
     * Conversion Document -> Booking.
     */
    private Booking toBooking(Document doc) {
        if (doc == null) return null;

        Booking b = new Booking();
        
        b.setId(doc.getObjectId("_id"));
        b.setRideId(doc.getObjectId("rideId"));
        b.setPassengerId(doc.getObjectId("passengerId"));
        b.setSeats(doc.getInteger("seats", 0));
        // pour persister le statut et le code du ticket
        b.setTicketCode(doc.getString("ticketCode"));
        b.setStatus(doc.getString("status"));
        // reponse du coducteur
        b.setDriverMessage(doc.getString("driverMessage"));
        // information pour le sous-trajet
        b.setFromCity(doc.getString("fromCity"));
        b.setToCity(doc.getString("toCity"));
        b.setFromIndex(doc.getInteger("fromIndex", -1));
        b.setToIndex(doc.getInteger("toIndex", -1));
        b.setPricePerSeat(doc.getInteger("pricePerSeat", 0));
        b.setTotalPrice(doc.getInteger("totalPrice", 0));

        Object created = doc.getDate("createdAt");
        /*if (created != null) {
            /*LocalDateTime ldt = LocalDateTime.ofInstant(created.toInstant(), zoneId);
            b.setCreatedAt(ldt);
            b.setCreatedAt(DateTimeUtils.toLocalDateTime(created));

        }*/
        if (created instanceof java.time.LocalDateTime ldt) {
            b.setCreatedAt(ldt);
        } else if (created instanceof java.util.Date d) {
            b.setCreatedAt(d.toInstant()
                           .atZone(java.time.ZoneId.systemDefault())
                           .toLocalDateTime());
        } else if (created instanceof String s) {
            // si tu as stocké une string ISO
            b.setCreatedAt(LocalDateTime.parse(s));
        }

        return b;
    }
    
    /*
     * Méthode qui transforme un Booking en Document
     */
    private Document toDocument(Booking b) {
        Document doc = new Document();

        // 
        doc.append("rideId", b.getRideId());
        doc.append("passengerId", b.getPassengerId());
        doc.append("seats", b.getSeats());
        doc.append("status", b.getStatus());
        doc.append("createdAt", b.getCreatedAt());

        //  champ pour les spus trajets
        doc.append("fromCity", b.getFromCity());
        doc.append("toCity", b.getToCity());
        doc.append("fromIndex", b.getFromIndex());
        doc.append("toIndex", b.getToIndex());
        doc.append("pricePerSeat", b.getPricePerSeat());
        doc.append("totalPrice", b.getTotalPrice());

        return doc;
    }

    

    /**
     * Insère une nouvelle réservation.
     */
    public void insert(Booking booking) {
        Date createdDate = DateTimeUtils.toDate(booking.getCreatedAt());

        /*Date createdDate = null;
        if (booking.getCreatedAt() != null) {
            createdDate = Date.from(booking.getCreatedAt().atZone(zoneId).toInstant());
        }
       */
        Document doc = new Document();
        	
        if (booking.getId() != null) {
        	doc.put("_id", booking.getId());
        }
        doc.append("rideId", booking.getRideId());
        doc.append("passengerId", booking.getPassengerId());
        doc.append("seats", booking.getSeats());
        doc.append("createdAt", createdDate);
		doc.append("ticketCode", booking.getTicketCode());
		doc.append("status", booking.getStatus());
		doc.append("driverMessage", booking.getDriverMessage());

		doc.put("fromIndex", booking.getFromIndex());
	    doc.put("toIndex", booking.getToIndex());
	    doc.put("fromCity", booking.getFromCity());
	    doc.put("toCity", booking.getToCity());
	    doc.put("pricePerSeat", booking.getPricePerSeat());
	    doc.put("totalPrice", booking.getTotalPrice());		

        col.insertOne(doc);
        booking.setId(doc.getObjectId("_id"));
    }

    /**
     * Retourne toutes les réservations d'un passager donné.
     */
    public List<Booking> findByPassengerId(ObjectId passengerId) {
        List<Booking> result = new ArrayList<>();
        for (Document doc : col.find(eq("passengerId", passengerId))) {
            result.add(toBooking(doc));
        }
        return result;
    }

    /**
     * Calcule le nombre total de places déjà réservées pour un trajet donné.
     * Utile pour vérifier la disponibilité.
     */
    public int countSeatsForRide(ObjectId rideId) {
        int total = 0;
        for (Document doc: col.find(eq("rideId", rideId))) {
            Integer seats = doc.getInteger("seats");
            if (seats != null) {
                total += seats;
            }
        }
        return total;
    }
    
    //
    public int countConfirmedSeatsForRide(ObjectId rideId) {
        int total = 0;

        for (Document doc : col.find(and(
                eq("rideId", rideId),
                eq("status", "CONFIRMED") // on ne compte que les réservations confirmées
        ))) {
            Integer seats = doc.getInteger("seats");
            if (seats != null) {
                total += seats;
            }
        }
        return total;
    }
    
    // recherche via id
    public Booking findById(ObjectId id) {
        Document doc = col.find(eq("_id", id)).first();
        return toBooking(doc);
    }

    public List<Booking> findByRideId(ObjectId rideId) {
        List<Booking> result = new ArrayList<>();
        for (Document doc : col.find(eq("rideId", rideId))) {
        	Booking b=toBooking(doc);
        	if(b!=null) {
               result.add(b);
        	}
        }
        return result;
    }

    /**
     * Met à jour certains champs de la réservation.
     */
    public void updateBooking(ObjectId id, String status, String driverMessage, String ticketCode) {
        Document updateDoc = new Document();
        if (status != null) {
            updateDoc.append("status", status);
        }
        if (driverMessage != null) {
            updateDoc.append("driverMessage", driverMessage);
        }
        if (ticketCode != null) {
            updateDoc.append("ticketCode", ticketCode);
        }
        if (!updateDoc.isEmpty()) {
            col.updateOne(eq("_id", id), new Document("$set", updateDoc));
        }
    }
}
