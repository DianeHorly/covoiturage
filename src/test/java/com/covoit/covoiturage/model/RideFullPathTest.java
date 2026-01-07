package com.covoit.covoiturage.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests concrets sur la construction du chemin complet (fullPath)
 * et le calcul des arrêts intermédiaires.
 */
class RideFullPathTest {

    /**
     * Cas 1:
     * - departureCity = "paris"
     * - arrivalCity = "madrid"
     * - stops = ["poitiers", "bordeaux"]
     *
     * 	 -> getFullPath() doit renvoyer :
     *   ["Paris", "Poitiers", "Bordeaux", "Madrid"]
     *   (avec ajout du départ et de l'arrivée, formatés proprement)
     */
    @Test
    void givenStopsWithoutDepArr_whenGetFullPath_thenDepArrAreAdded() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("madrid");

        // stops ne contient que les villes intermédiaires
        ride.setStops(Arrays.asList("poitiers", "bordeaux"));

        List<String> path = ride.getFullPath();

        assertEquals(
                Arrays.asList("Paris", "Poitiers", "Bordeaux", "Madrid"),
                path
        );
    }

    /**
     * Cas 2:
     * - departureCity = "paris"
     * - arrivalCity = "madrid"
     * - stops = ["paris", "poitiers", "madrid"]
     *
     * -> getFullPath() ne doit pas dupliquer le départ/arrivée:
     *   ["Paris", "Poitiers", "Madrid"]
     */
    @Test
    void givenStopsAlreadyContainDepArr_whenGetFullPath_thenNoDuplicates() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("madrid");

        // stops contient déjà départ et arrivée
        ride.setStops(Arrays.asList("paris", "poitiers", "madrid"));

        List<String> path = ride.getFullPath();

        assertEquals(
                Arrays.asList("Paris", "Poitiers", "Madrid"),
                path
        );
    }

    /**
     * Cas 3:
     * - fullPath = [Paris, Poitiers, Bordeaux, Madrid]
     *   -> il y a 2 arrêts intermédiaires (Poitiers, Bordeaux)
     *
     * getIntermediateStopsCount() doit renvoyer 2.
     */
    @Test
    void givenFullPath_whenGetIntermediateStopsCount_thenReturnCorrectNumber() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("madrid");
        ride.setStops(Arrays.asList("paris", "poitiers", "bordeaux", "madrid"));

        int count = ride.getIntermediateStopsCount();

        assertEquals(2, count);
    }
    
}
