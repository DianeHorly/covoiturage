package com.covoit.covoiturage.service;

import com.covoit.covoiturage.dao.RideDao;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.util.BusinessException;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Logique métier pour les trajets (Ride).
 * - validation des données
 * - appel au DAO pour lire/écrire en base
 */
public class RideService {

    private final RideDao rideDao = new RideDao();

    /**
     * Création d'un trajet.
     *
     * @param driver        utilisateur conducteur (doit être connecté)
     * @param departureCity ville de départ saisie dans le formulaire
     * @param arrivalCity   ville d'arrivée saisie dans le formulaire
     * @param dateTime      date/heure de départ
     * @param totalSeats    nombre total de places
     * @param pricePerSeat  prix par place (trajet complet)
     * @param description   commentaire libre
     * @param stopsRaw      texte brut des arrêts, tel que saisi dans le textarea
     *                      (une ville par ligne, ou séparée par ; ou ,)
     * @param segmentPrices liste des prix par segment (peut contenir des null / être plus courte),
     *                      un prix par tronçon entre deux villes consécutives.
     */
    public void createRide(User driver,
                           String departureCity,
                           String arrivalCity,
                           LocalDateTime dateTime,
                           int totalSeats,
                           int pricePerSeat,
                           String description,
                           String stopsRaw,
                           List<Integer> segmentPrices) throws BusinessException {

        // -----    Sécurité: verifie si le conducteur est connecté ?   ------
        if (driver == null || driver.getId() == null) {
            throw new BusinessException("Vous devez être connecté pour créer un trajet.");
        }

        // --------     Validation villes      ----------
        if (departureCity == null || departureCity.isBlank()) {
            throw new BusinessException("La ville de départ est obligatoire.");
        }
        if (arrivalCity == null || arrivalCity.isBlank()) {
            throw new BusinessException("La ville d'arrivée est obligatoire.");
        }

        
        String dep = departureCity.trim();
        String arr = arrivalCity.trim();
        // Normalisation (minuscule, trim)
        String depNorm = Ride.normalizeCity(departureCity);
        String arrNorm = Ride.normalizeCity(arrivalCity);

        if (depNorm == null || arrNorm == null) {
            throw new BusinessException("Les villes de départ et d'arrivée sont obligatoires.");
        }

        // ----------     Validation date / heure       -----------------
        if (dateTime == null) {
            throw new BusinessException("La date et l'heure de départ sont obligatoires.");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("La date de départ doit être dans le futur.");
        }

        // ---------    Validation nombre de places / prix   -----------
        if (totalSeats <= 0) {
            throw new BusinessException("Le nombre de places doit être supérieur à zéro.");
        }
        if (pricePerSeat < 0) {
            throw new BusinessException("Le prix par place ne peut pas être négatif.");
        }

        // --------     Construction de la liste d'arrêts ------------
        //List<String> stopsToStore = new ArrayList<>();
        
        List<String> fullPathForSegments = new ArrayList<>();

        // départ en premier
        fullPathForSegments.add(dep);

     // arrêts saisis dans le textarea (on accepte \n, ;, , comme séparateurs)
        if (stopsRaw != null && !stopsRaw.isBlank()) {
            String normalizedRaw = stopsRaw
                    .replace(";", "\n")
                    .replace(",", "\n");

            String[] lines = normalizedRaw.split("\\r?\\n");
            for (String line : lines) {
                String city = line.trim();
                if (city.isEmpty()) continue;

                // on évite d'ajouter deux fois de suite la même ville
                if (!fullPathForSegments.isEmpty()
                        && city.equalsIgnoreCase(fullPathForSegments.get(fullPathForSegments.size() - 1))) {
                    continue;
                }
                fullPathForSegments.add(city);
            }
        }

        // arrivée en dernier (en évitant le doublon si déjà présente)
        String last = fullPathForSegments.get(fullPathForSegments.size() - 1);
        if (!arr.equalsIgnoreCase(last)) {
            fullPathForSegments.add(arr);
        }

        // nombre de tronçons= nombre de villes - 1
        int nbSegments = Math.max(0, fullPathForSegments.size() - 1);

        // =====================================================================
        // 	  On construit la liste des ARRÊTS INTERMÉDIAIRES à stocker dans Ride
        //    -> stops = toutes les villes sauf la première (départ) et la dernière (arrivée)
        // =====================================================================
        List<String> stopsToStore = new ArrayList<>();
        if (fullPathForSegments.size() > 2) {
            for (int i = 1; i < fullPathForSegments.size() - 1; i++) {
                stopsToStore.add(fullPathForSegments.get(i));
            }
        }

        // =====================================================================
        //    On aligne les PRIX PAR SEGMENT: un prix par tronçon
        //    (segment 0 = ville[0] -> ville[1], etc...)
        // =====================================================================
        List<Integer> finalSegmentPrices = new ArrayList<>();

        if (nbSegments > 0) {
            for (int i = 0; i < nbSegments; i++) {
                Integer p = null;

                if (segmentPrices != null && i < segmentPrices.size()) {
                    p = segmentPrices.get(i); // peut être null si champ laissé vide
                }

                if (p != null && p < 0) {
                    throw new BusinessException("Les prix par segment ne peuvent pas être négatifs.");
                }

                // on stocke même les prix null: null signifie "utiliser le prix global"
                finalSegmentPrices.add(p);
            }
        }

        // =====================================================================
        //  Construction de l'objet Ride et sauvegarde
        // =====================================================================
        Ride ride = new Ride();
        ride.setDriverId(driver.getId());
        ride.setDepartureCity(dep);   // on garde la forme saisie, le DAO normalise pour la base
        ride.setArrivalCity(arr);
        ride.setDepartureDateTime(dateTime);
        ride.setTotalSeats(totalSeats);
        ride.setPricePerSeat(pricePerSeat);
        ride.setDescription(description);
        ride.setStops(stopsToStore);              // uniquement les arrêts intermédiaires
        ride.setSegmentPrices(finalSegmentPrices); // le prix par segment

        // Persistance Mongo
        rideDao.insert(ride);
    }

    /**
     * Récupère un trajet par son id (String).
     */
    public Ride getById(String rideIdStr) throws BusinessException {
        if (rideIdStr == null || rideIdStr.isBlank()) {
            throw new BusinessException("Identifiant de trajet manquant.");
        }

        ObjectId id;
        try {
            id = new ObjectId(rideIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Identifiant de trajet invalide.");
        }

        Ride ride = rideDao.findById(id);
        if (ride == null) {
            throw new BusinessException("Trajet introuvable.");
        }
        return ride;
    }

    /**
     * Recherche des trajets selon ville de départ, ville d'arrivée et date.
     */
    public List<Ride> searchRides(String departureCity,
                                  String arrivalCity,
                                  LocalDate date) throws BusinessException {
        if (departureCity == null || departureCity.isBlank()
                || arrivalCity == null || arrivalCity.isBlank()
                || date == null) {
            throw new BusinessException("Merci de renseigner départ, arrivée et date.");
        }

        return rideDao.searchByCitiesAndDate(departureCity, arrivalCity, date);
    }

    /**
     * Derniers trajets pour l'accueil ou la liste simple.
     */
    public List<Ride> getLatestRides(int limit) {
        if (limit <= 0) limit = 10;
        return rideDao.findLatest(limit);
    }
}