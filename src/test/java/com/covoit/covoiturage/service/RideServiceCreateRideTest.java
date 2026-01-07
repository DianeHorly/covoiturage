package com.covoit.covoiturage.service;

import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.util.BusinessException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests de validation pour RideService.createRide(...)
 * On vérifie que les mauvaises données lèvent bien une BusinessException.
 */
public class RideServiceCreateRideTest {

    private RideService rideService;
    private User driver;
    private LocalDateTime futureDateTime;

    @BeforeEach
    void setUp() {
        // service réel
        rideService = new RideService();

        // conducteur: on a seulement besoin de l'id
        driver = new User();
        driver.setId(new ObjectId());

        // date dans le futur (pour les tests qui n'ont pas besoin de "date invalide")
        futureDateTime = LocalDateTime.now().plusDays(1);
    }

    @Test
    void createRide_whenDepartureCityMissing_shouldThrow() {
        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        driver,
                        null,                      // departureCity manquante
                        "Lyon",                    // arrivalCity
                        futureDateTime,            // date future
                        3,                         // totalSeats
                        20,                        // pricePerSeat
                        "test",                    // description
                        null,                      // stopsRaw
                        null                       // segmentPrices
                )
        );
    }

    @Test
    void createRide_whenArrivalCityMissing_shouldThrow() {
        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        driver,
                        "Paris",                   // departureCity
                        "   ",                     // arrivalCity vide
                        futureDateTime,
                        3,
                        20,
                        "test",
                        null,
                        null
                )
        );
    }

    @Test
    void createRide_whenDateInPast_shouldThrow() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        driver,
                        "Paris",
                        "Lyon",
                        past,                     // date passée -> invalide
                        3,
                        20,
                        "test",
                        null,
                        null
                )
        );
    }

    @Test
    void createRide_whenTotalSeatsNotPositive_shouldThrow() {
        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        driver,
                        "Paris",
                        "Lyon",
                        futureDateTime,
                        0,                        // 0 places -> invalide
                        20,
                        "test",
                        null,
                        null
                )
        );
    }

    @Test
    void createRide_whenPriceNegative_shouldThrow() {
        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        driver,
                        "Paris",
                        "Lyon",
                        futureDateTime,
                        3,
                        -5,                       // prix négatif -> invalide
                        "test",
                        null,
                        null
                )
        );
    }

    @Test
    void createRide_whenDriverNotConnected_shouldThrow() {
        // driver null -> pas connecté
        assertThrows(
                BusinessException.class,
                () -> rideService.createRide(
                        null,
                        "Paris",
                        "Lyon",
                        futureDateTime,
                        3,
                        20,
                        "test",
                        null,
                        null
                )
        );
    }
}
