package com.covoit.covoiturage.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RidePriceTest {

    /**
     * Cas 1: aucun prix de segment défini
     *  alors on doit retomber sur pricePerSeat.
     */
    @Test
    void givenNoSegmentPrices_whenAskSubRidePrice_thenUseGlobalPrice() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("aéroport de madrid");
        ride.setStops(Arrays.asList("paris", "poitiers", "bordeaux", "aéroport de madrid"));

        ride.setPricePerSeat(100);            // prix global
        ride.setSegmentPrices(null);          // pas de grille de prix car il est géré par javascript

        // sous-trajet Paris -> Poitiers (indices 0 -> 1)
        Integer price = ride.getPricePerSeatForSegment(0, 1);

        assertNotNull(price);
        assertEquals(100, price);
    }

    /**
     * Cas 2: une grille de prix par segment est définie.
     * - Paris -> Poitiers: 40
     * - Poitiers -> Bordeaux: 30
     * - Bordeaux -> Madrid: 30
     *
     * - Paris -> Poitiers => 40
     * - Poitiers -> Bordeaux => 30
     * - Bordeaux -> Madrid => 30
     * - Paris -> Bordeaux => 40 + 30 = 70
     * - Paris -> Madrid => 40 + 30 + 30 = 100
     */
    @Test
    void givenSegmentPrices_whenAskSubRidePrice_thenUseSumOfSegments() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("aéroport de madrid");
        ride.setStops(Arrays.asList("paris", "poitiers", "bordeaux", "aéroport de madrid"));

        ride.setPricePerSeat(100); // prix global (fallback)
        ride.setSegmentPrices(Arrays.asList(40, 30, 30));

        // Paris -> Poitiers (0 -> 1)
        assertEquals(40, ride.getPricePerSeatForSegment(0, 1));

        // Poitiers -> Bordeaux (1 -> 2)
        assertEquals(30, ride.getPricePerSeatForSegment(1, 2));

        // Bordeaux -> Madrid (2 -> 3)
        assertEquals(30, ride.getPricePerSeatForSegment(2, 3));

        // Paris -> Bordeaux (0 -> 2) = 40 + 30
        assertEquals(70, ride.getPricePerSeatForSegment(0, 2));

        // Paris -> Madrid (0 -> 3) = 40 + 30 + 30
        assertEquals(100, ride.getPricePerSeatForSegment(0, 3));
    }

    /**
     * Cas 3: indices invalides alors on doit retourner null.
     */
    @Test
    void givenInvalidIndices_whenAskSubRidePrice_thenReturnNull() {
        Ride ride = new Ride();
        ride.setDepartureCity("paris");
        ride.setArrivalCity("aéroport de madrid");
        ride.setStops(Arrays.asList("paris", "poitiers", "bordeaux", "aéroport de madrid"));
        ride.setPricePerSeat(100);

        // fromIndex négatif
        assertNull(ride.getPricePerSeatForSegment(-1, 2));

        // toIndex <= fromIndex
        assertNull(ride.getPricePerSeatForSegment(2, 2));
        assertNull(ride.getPricePerSeatForSegment(2, 1));

        // toIndex > nbSegments
        // nbSegments = 3 (0->1, 1->2, 2->3), donc max toIndex = 3
        assertNull(ride.getPricePerSeatForSegment(0, 4));
    }
}
