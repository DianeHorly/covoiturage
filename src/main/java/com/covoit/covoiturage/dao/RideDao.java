package com.covoit.covoiturage.dao;

import com.covoit.covoiturage.config.MongoManager;
import com.covoit.covoiturage.model.Ride;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
//import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import com.covoit.covoiturage.util.DateTimeUtils;


/**
 * DAO pour la collection "rides" (trajet).
 * Ne contient QUE la logique de persistance.
 */
public class RideDao {

    private final MongoCollection<Document> col;
    // Zone utilisée pour convertir Date <-> LocalDateTime 
   // private final ZoneId zoneId = ZoneId.systemDefault();

    public RideDao() {
        MongoDatabase db = MongoManager.getDatabase();
        this.col = db.getCollection("rides");
    }

    /**
     * Convertit un Document Mongo en objet Ride.
     * (C'est la "traduction" base de données -> Java)
     */
    private Ride toRide(Document doc) {
        if (doc == null) return null;

        Ride r = new Ride();
        
        // Identifiants
        r.setId(doc.getObjectId("_id"));
        r.setDriverId(doc.getObjectId("driverId"));
        
        // Villes de départ/arrivée stockées en base normalisées en minuscule
        r.setDepartureCity(doc.getString("departureCity"));
        r.setArrivalCity(doc.getString("arrivalCity"));

        // Date/heure de départ stockée en java.util.Date dans Mongo
        Date date = doc.getDate("departureDateTime");
        if (date != null) {
            /*LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), zoneId);
            r.setDepartureDateTime(ldt);*/
            r.setDepartureDateTime(DateTimeUtils.toLocalDateTime(date));

        }

        r.setDepartureDateTime(DateTimeUtils.toLocalDateTime(date));

        r.setTotalSeats(doc.getInteger("totalSeats", 0));
        r.setPricePerSeat(doc.getInteger("pricePerSeat", 0));
        r.setDescription(doc.getString("description"));
        
        // Liste des arrêts éventuellement stockée en base
        List<String> stops = doc.getList("stops", String.class);
        if (stops != null) {
            r.setStops(stops);   //le setter gère le cas null-> nouvelle ArrayList
        }
        
        // liste des prix par segment
        List<Integer> segPrices = doc.getList("segmentPrices", Integer.class);
        if (segPrices != null) {
            r.setSegmentPrices(segPrices);
        }
        
        return r;
    }

    /**
     * Insère un trajet dans la BD(base)
     * java-> Mongo
     */
    public void insert(Ride ride) {
    	// Conversion LocalDateTime en java.util.Date pour Mongo
        Date date = DateTimeUtils.toDate(ride.getDepartureDateTime());
        
        // Normalisation des villes pour la base (tout en minuscule).
        String depNorm = Ride.normalizeCity(ride.getDepartureCity());
        String arrNorm = Ride.normalizeCity(ride.getArrivalCity());

        // Document à inserer
        Document doc = new Document()
                .append("driverId", ride.getDriverId())
                .append("departureCity",depNorm)
                .append("arrivalCity", arrNorm )
                .append("departureDateTime", date)
                .append("totalSeats", ride.getTotalSeats())
                .append("pricePerSeat", ride.getPricePerSeat())
                .append("description", ride.getDescription())
                .append("stops", ride.getStops());
        
        // verifie et enregistre le prix par segment
        if (ride.getSegmentPrices() != null) {
            doc.put("segmentPrices", ride.getSegmentPrices());
        }

        // Insertion en base
        col.insertOne(doc);
        // On met à jour l'id dans l'objet métier après l'insert
        ride.setId(doc.getObjectId("_id"));
    }
    
    /**
     * Récupère un trajet par son id MongoDB (ObjectId).
     */
    public Ride findById(ObjectId id) {
        if (id == null) {
            return null;
        }
        Document doc = col.find(eq("_id", id)).first();
        return toRide(doc);
    }
    

    /**
     * Récupère un trajet par son id MongoDB (String).
     */
    public Ride findById(String idStr) {
        if (idStr == null || idStr.isBlank()) {
            return null;
        }

        ObjectId id;
        try {
            id = new ObjectId(idStr);
        } catch (IllegalArgumentException e) {
            // id non valide
            return null;
        }

        Document doc = col.find(eq("_id", id)).first();
        return toRide(doc);
    }

    

    /**
     * Recherche des trajets par ville et date du jour, INSENSIBLE à la casse.
     *
     *  - filtre Mongo uniquement sur la date de départ
     *  - reconstruit le chemin complet du trajet (départ, arrêts et arrivée)
     *  - normalise tout en minuscule
     *  - vérifie que:
     *      * le chemin contient la ville de départ recherchée
     *      * le chemin contient la ville d'arrivée recherchée
     *      * l'arrivée est après le départ dans ce chemin
     */
     public List<Ride> searchByCitiesAndDate(String departureCity,
                                            String arrivalCity,
                                            LocalDate date) {

        // on normalise les villes saisies par l'utilisateur (minuscules)
        String fromNorm = Ride.normalizeCity(departureCity);
        String toNorm   = Ride.normalizeCity(arrivalCity);

        // plage de dates (jour donné)
        Date start = DateTimeUtils.startOfDay(date);
        Date end   = DateTimeUtils.startOfNextDay(date);

        List<Ride> result = new ArrayList<>();

        // On ne filtre en base QUE sur la date
        for (Document doc : col.find(and(
                gte("departureDateTime", start),
                lt("departureDateTime", end)
        ))) {
            Ride ride = toRide(doc);
            if (ride == null) continue;

            // On récupère le chemin complet du trajet
            List<String> path = ride.getFullPath();
            if (path == null || path.isEmpty()) {
                continue;
            }

            // on normalise toutes les villes du chemin en minuscule
            List<String> normalizedPath = new ArrayList<>();
            for (String city : path) {
                String n = Ride.normalizeCity(city); // trim et toLowerCase
                if (n != null) {
                    normalizedPath.add(n);
                }
            }
            if (normalizedPath.isEmpty()) {
                continue;
            }

            // On cherche les positions dans ce chemin
            int fromIndex;
            int toIndex;

            if (fromNorm != null) {
                fromIndex = normalizedPath.indexOf(fromNorm);
                if (fromIndex < 0) {
                    // le trajet ne passe pas par la ville de départ recherchée
                    continue;
                }
            } else {
                // Si pas de ville de départ saisie => début du trajet
                fromIndex = 0;
            }

            if (toNorm != null) {
                toIndex = normalizedPath.lastIndexOf(toNorm);
                if (toIndex < 0) {
                    // le trajet ne passe pas par la ville d'arrivée recherchée
                    continue;
                }
            } else {
                // pas de ville d'arrivée saisie => fin du trajet
                toIndex = normalizedPath.size() - 1;
            }

            // l'arrivée doit être APRÈS le départ
            if (toIndex <= fromIndex) {
                continue;
            }

            // ce trajet correspond à la recherche
            result.add(ride);
        }

        return result;
    }

    /**
     * Liste les trajets les plus récents (par exemple pour la page d'accueil).
     * Ici on trie par date de départ croissante.
     */
    public List<Ride> findLatest(int limit) {
        List<Ride> result = new ArrayList<>();
        for (Document doc:col.find()
                .sort(new Document("departureDateTime", 1)) 
                .limit(limit)) {
            result.add(toRide(doc));
        }
        return result;
    }
    
    // Récupère tous les trajets d'un conducteur donné.
    public List<Ride> findByDriverId(ObjectId driverId) {
        List<Ride> result = new ArrayList<>();
        for (Document doc : col.find(eq("driverId", driverId))) {
            result.add(toRide(doc));
        }
        return result;
    }
    


}
