package com.covoit.covoiturage.model;

import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un trajet de covoiturage.
 * Un trajet contient des arrets:
 *  - departureCity: première ville du trajet.
 *  - arrivalCity: dernière ville du trajet.
 *  - stops: toutes les villes du parcours dans l'ordre.
 */
public class Ride {

    private ObjectId id;                 // id MongoDB
    private ObjectId driverId;           // id de l'utilisateur conducteur
    private String departureCity;        // ville de départ
    private String arrivalCity;          // ville d'arrivée
    private LocalDateTime departureDateTime; //  date/heure de départ
    private int totalSeats;              // nombre total de places
    private int pricePerSeat;            // prix par place (en euros) pour le trajet complet
    private String description;          // commentaire ou description
    
    // Prix par segment (entre deux villes consécutives du fullPath)
    // segmentPrices.get(0) = prix entre ville[0] et ville[1], etc.
    private List<Integer> segmentPrices = new ArrayList<>();
    
    // Liste des arrets
    private List<String> stops = new ArrayList<>();
    
    private static final DateTimeFormatter DISPLAY_FMT =DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    
    
    //---------------     Getters et setters     ------------------------
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getDriverId() {
        return driverId;
    }

    public void setDriverId(ObjectId driverId) {
        this.driverId = driverId;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(LocalDateTime departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getPricePerSeat() {
        return pricePerSeat;
    }

    public void setPricePerSeat(int pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    // arrets
    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        // on évite une liste null pour simplifier l'utilisation dans les JSP
        if (stops == null) {
            this.stops = new ArrayList<>();
        } else {
            this.stops = stops;
        }
    }
    
    
    /**
     * Retourne la liste des arrêts intermédiaires (sans départ ni arrivée),
     * sous forme de noms déjà formatés.
     */
    public List<String> getIntermediateStops() {
        List<String> full = getFullPath();
        List<String> result = new ArrayList<>();

        if (full.size() <= 2) {
            // 0 ou 1 ville -> pas d'arrêt intermédiaire
            return result;
        }

        // On prend tout ce qui est entre la 1ère (départ) et la dernière (arrivée)
        for (int i = 1; i < full.size() - 1; i++) {
            result.add(full.get(i));
        }
        return result;
    }

    
    //  Indique si le trajet a un nombre de places strictement positif. 
    public boolean hasAvailableSeats() {
        return totalSeats > 0;
    }
    
    //  Date/heure formatée pour l'affichage dans les JSP.
    public String getDepartureDateTimeFormatted() {
        if (departureDateTime == null) return "";
        return departureDateTime.format(DISPLAY_FMT);
    }

    public String getDepartureCityDisplay() {
        return formatCityName(departureCity);
    }

    public String getArrivalCityDisplay() {
        return formatCityName(arrivalCity);
    }

    
    /**
     * Retourne l'itinéraire complet sous frome de liste des arrêts  séparés par " -> ".
	 *
     */
    public String getStopsDisplay() {
        List<String> full = getFullPath();
        if (full.isEmpty()) {
            return "";
        }
        return String.join(" -> ", full);
    }
    
    
    // --- Helpers d'affichage pour les arrêts ------------------------
    // Nombre d'arrêts intermédiaires (sans compter départ ni arrivée).
    public int getIntermediateStopsCount() {
        List<String> full = getFullPath();
        int n = full.size() - 2;
        return Math.max(0, n);
    }
    
    
    /**
     * Retourne un libellé du type:
     *  - "Sans arrêt"
     *  - "1 arrêt"
     *  - "3 arrêts"
     *
     * Un "arrêt" = une ville intermédiaire, on NE COMPTE PAS
     * la ville de départ ni la ville d'arrivée.
     */
    public String getStopsCountLabel() {
        int nbStops = getIntermediateStopsCount();

        if (nbStops <= 0) {
            return "Sans arrêt";
        }
        if (nbStops == 1) {
            return "1 arrêt";
        }
        return nbStops + " arrêts";
    }


    /**
     * Retourne la liste complète des villes du trajet, dans l'ordre:
     *   départ -> arrêts intermédiaires -> arrivée
     * en utilisant les noms formatés pour l'affichage.
	 */
    public List<String> getFullPath() {
        List<String> path = new ArrayList<>();

        String depNorm = normalizeCity(departureCity);
        String arrNorm = normalizeCity(arrivalCity);

        // On part de la liste des stops
        if (stops != null) {
            for (String s : stops) {
                String norm = normalizeCity(s);
                if (norm == null) continue;

                String formatted = formatCityName(s);

                // On évite les doublons consécutifs
                if (!path.isEmpty()
                        && path.get(path.size() - 1).equalsIgnoreCase(formatted)) {
                    continue;
                }

                path.add(formatted);
            }
        }

        // On s'assure que la 1ère ville est bien la ville de départ.
        if (depNorm != null) {
            if (path.isEmpty()) {
                path.add(formatCityName(departureCity));
            } else {
                String firstNorm = normalizeCity(path.get(0));
                if (!depNorm.equals(firstNorm)) {
                    path.add(0, formatCityName(departureCity));
                }
            }
        }

        // On s'assure que la dernière ville est bien la ville d'arrivée.
        if (arrNorm != null) {
            if (path.isEmpty()) {
                path.add(formatCityName(arrivalCity));
            } else {
                String lastNorm = normalizeCity(path.get(path.size() - 1));
                if (!arrNorm.equals(lastNorm)) {
                    path.add(formatCityName(arrivalCity));
                }
            }
        }

        return path;
    }
  
    
    /**
     * Normalisation du nom de ville :
     *  - trim
     *  - Première lettre en majuscule, le reste en minuscule.
     */
    private String formatCityName(String city) {
        if (city == null) return "";
        String c = city.trim();
        if (c.isEmpty()) return "";
        return c.substring(0, 1).toUpperCase() + c.substring(1).toLowerCase();
    }

    /**
     * Normalisation (tout en minuscule).
     */
    public static String normalizeCity(String city) {
        if (city == null) return null;
        String c = city.trim().toLowerCase();
        return c.isEmpty() ? null : c;
    }
    
    
    // helper pour retrouver l'index d'une ville dans le chemin complet
    public int indexOfCityInFullPath(String city) {
        if (city == null) 
        	return -1;
        String target = normalizeCity(city);

        java.util.List<String> path = getFullPath();
        for (int i = 0; i < path.size(); i++) {
            String c = path.get(i);
            if (normalizeCity(c) != null && normalizeCity(c).equals(target)) {
                return i;
            }
        }
        return -1;
    }
    
 // -------------------------------------------------------------------
    // PRIX DES SOUS-TRAJETS
    // -------------------------------------------------------------------

    /**
     * Liste des prix par segment.
     * Exemple: pour un trajet Paris -> Poitiers -> Bordeaux -> Madrid
     * il y a 3 tronçons :
     *   0 : Paris -> Poitiers
     *   1 : Poitiers -> Bordeaux
     *   2 : Bordeaux -> Madrid
     *   
     */    
    public List<Integer> getSegmentPrices() {
        if (segmentPrices == null) {
            segmentPrices = new ArrayList<>();
        }
        return segmentPrices;
    }
    public void setSegmentPrices(List<Integer> segmentPrices) {
        if (segmentPrices == null) {
            this.segmentPrices = new ArrayList<>();
        } else {
        	// on copie pour éviter les modifications externes
            this.segmentPrices = new ArrayList<>(segmentPrices);
        }
    }


    
    
    /**
     * Prix par place pour le sous-trajet allant de la ville d'indice fromIndex
     * à la ville d'indice toIndex (indices dans getFullPath()).
     *
     * Exp:
     * fullPath = [Nice, Cannes, Monaco, Gênes, Milan]
     * segments: 0:Nice->Cannes, 1:Cannes->Monaco, 2:Monaco->Gênes, 3:Gênes->Milan
     *
     * - trajet complet: fromIndex=0, toIndex=4
     * - sous-trajet Cannes->Monaco: fromIndex=1, toIndex=2
     *
     * Si aucun prix détaillé n'est défini, on renvoie pricePerSeat.
     */
    public Integer getPricePerSeatForSegment(int fromIndex, int toIndex) {
        List<String> path = getFullPath();
        if (path == null || path.size() < 2) {
            return null;
        }

        int nbSegments = path.size() - 1; // nombre de tronçons entre les villes

        // mêmes règles d'indices que dans getRemainingSeatsForSegment
        if (fromIndex < 0 || toIndex <= fromIndex || toIndex > nbSegments) {
            return null;
        }

        // Pas de grille de prix => tout le monde paie le même prix
        if (segmentPrices == null || segmentPrices.size() != nbSegments) {
            return pricePerSeat;
        }

        List<Integer> prices = getSegmentPrices(); // jamais null

        // Si aucun prix de segment n'a été saisi, on applique un prix global
        // identiqué pour chaque tronçon du sous-trajet.
        if (prices.isEmpty()) {
            int segCount = toIndex - fromIndex;
            return segCount * pricePerSeat;
        }

        
        int total = 0;
        // seg est l'indice du TRONÇON (entre ville[seg] et ville[seg+1])
        for (int seg = fromIndex; seg < toIndex; seg++) {
        	
        	// si la liste est plus courte que le nombre de segments,
            // on lit "null" et on retombera sur le prix global.
            Integer segPrice = (seg < prices.size() ? prices.get(seg) : null);
            
            // Si pas de prix ou prix <= 0 alors on utilise pricePerSeat
            if (segPrice == null || segPrice <= 0) {
                segPrice = pricePerSeat; // fallback
            }
            total += segPrice;
        }
        return total;
    }

    // 
    public int computeUnitPriceForSegment(int fromIndex, int toIndex) {
        Integer p = getPricePerSeatForSegment(fromIndex, toIndex);
        if (p == null || p <= 0) {
            return pricePerSeat;
        }
        return p;
    }
}