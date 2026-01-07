package com.covoit.covoiturage.service;

import com.covoit.covoiturage.dao.BookingDao;
import com.covoit.covoiturage.dao.RideDao;
import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.util.BusinessException;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Permet l'accès Mongo (collection bookings)
 * Logique métier pour les réservations (bookings).
 * vérifier que l'utilisateur est connecté,
 * vérifier que le trajet existe,
 * vérifier que le trajet n'est pas dans le passé,
 * vérifier qu'il reste assez de places (totalSeats - réservées >= demandées),
 * créer la réservation.
 */
public class BookingService {

    private final BookingDao bookingDao = new BookingDao();
    private final RideDao rideDao = new RideDao();

    /**
     * Réserve un trajet pour un passager.
     *
     * @param passenger utilisateur connecté
     * @param rideIdStr id du trajet (String)
     * @param seats nombre de places à réserver
     */
    public Booking bookRide(User passenger, String rideIdStr, int seats) throws BusinessException {

        if (passenger == null || passenger.getId() == null) {
            throw new BusinessException("Vous devez être connecté pour réserver un trajet.");
        }

        if (seats <= 0) {
            throw new BusinessException("Le nombre de places à réserver doit être supérieur à zéro.");
        }

        // Conversion du rideId en ObjectId
        ObjectId rideId;
        try {
            rideId = new ObjectId(rideIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Identifiant de trajet invalide.");
        }

        // Récupérer le trajet
        Ride ride = rideDao.findById(rideId);
        if (ride == null) {
            throw new BusinessException("Le trajet demandé n'existe pas.");
        }

     // Le conducteur ne peut pas réserver son propre trajet
        if (ride.getDriverId() != null
                && passenger != null
                && ride.getDriverId().equals(passenger.getId())) {
            throw new BusinessException("Tu ne peux pas réserver ton propre trajet.");
        }
        
        // Vérifier que la date de départ est dans le futur
        if (ride.getDepartureDateTime() != null &&
                ride.getDepartureDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Il n'est plus possible de réserver ce trajet (date dépassée).");
        }

        // Calcul des places déjà réservées et confirmé pour ce trajet
        int alreadyBooked = bookingDao.countConfirmedSeatsForRide(rideId);

        int totalSeats = ride.getTotalSeats();
        int remaining = totalSeats - alreadyBooked;

        if (seats > remaining) {
            throw new BusinessException("Il ne reste que " + remaining +
                    " place(s) disponible(s) pour ce trajet.");
        }

        // Tout est OK, on crée la réservation
        Booking booking = new Booking();
        booking.setRideId(rideId);
        booking.setPassengerId(passenger.getId());
        booking.setSeats(seats);
        booking.setCreatedAt(LocalDateTime.now());

        // statut en attente de validation
        booking.setStatus("PENDING");
        
	    // pas de ticketCode au début
	     booking.setTicketCode(null);
	     booking.setDriverMessage(null);
	     
       /* // on génère un code de ticket simple 
        String ticketCode = "T-" + System.currentTimeMillis() + "-" + passenger.getId();
        booking.setTicketCode(ticketCode);*/

        bookingDao.insert(booking);

        return booking;
    }
    
    /**
     * Retourne la liste des réservations pour un utilisateur donné.
     */
    public List<Booking> getBookingsForUser(User passenger) throws BusinessException {
        if (passenger == null || passenger.getId() == null) {
            throw new BusinessException("Utilisateur non connecté.");
        }
        return bookingDao.findByPassengerId(passenger.getId());
    }

    /**
     * Nombre de places restantes sur l'ensemble du trajet.
     * On prend le minimum de places disponible sur tous les segments.
     */
    public int getRemainingSeats(Ride ride) {
        if (ride == null || ride.getId() == null) {
            return 0;
        }

        // On ne compte que les réservations CONFIRMÉES
        int already = bookingDao.countConfirmedSeatsForRide(ride.getId());

        int remaining = ride.getTotalSeats() - already;
        return Math.max(0, remaining);
    }
    
    
    /*
     * Méthodes de confirmation et refus côté conducteu
     * */
    public void confirmBooking(User driver, String bookingIdStr, String message) throws BusinessException {
        if (driver == null || driver.getId() == null) {
            throw new BusinessException("Utilisateur conducteur invalide.");
        }

        ObjectId bookingId;
        try {
            bookingId = new ObjectId(bookingIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Identifiant de réservation invalide.");
        }

        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new BusinessException("Réservation introuvable.");
        }

        // Vérifier que le driver est bien le conducteur du trajet
        Ride ride = rideDao.findById(booking.getRideId());
        if (ride == null || ride.getDriverId() == null || !ride.getDriverId().equals(driver.getId())) {
            throw new BusinessException("Vous ne pouvez pas confirmer cette réservation.");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Cette réservation n'est plus en attente.");
        }
        
	     // --------   Contrôle des places au moment de la confirmation   ---------
	     // On travaille sur le trajet complet ou sous-trajet
	     List<String> path = ride.getFullPath();
	     int nbSegments = (path == null) ? 0 : path.size() - 1;
	
	     // Valeurs par défaut si jamais les indices n'ont pas été remplis
	     int fromIndex = booking.getFromIndex();
	     int toIndex   = booking.getToIndex();
	
	     // Si indices invalides (anciennes données), on considère le trajet complet
	     if (path != null && (fromIndex < 0 || toIndex <= fromIndex || toIndex > nbSegments)) {
	         fromIndex = 0;
	         toIndex   = nbSegments;
	     }
	
	     // Places encore libres sur ce sous-trajet (en ne comptant que les autres CONFIRMED)
	     int remainingOnSegment = getRemainingSeatsForSegment(ride, fromIndex, toIndex);
	
	     if (booking.getSeats() > remainingOnSegment) {
	         throw new BusinessException(
	             "Il ne reste plus assez de places pour confirmer cette réservation."
	         );
	     }


        // Génération du ticketCode à la confirmation
        String ticketCode = "T-" + System.currentTimeMillis() + "-" + booking.getId();

        String msg = (message == null || message.isBlank())
                ? "Votre réservation est confirmée. Merci d'être à l'heure."
                : message.trim();

        bookingDao.updateBooking(bookingId, "CONFIRMED", msg, ticketCode);
    }
    
    
    // rejet du trajet
    public void rejectBooking(User driver, String bookingIdStr, String message) throws BusinessException {
        if (driver == null || driver.getId() == null) {
            throw new BusinessException("Utilisateur conducteur invalide.");
        }

        ObjectId bookingId;
        try {
            bookingId = new ObjectId(bookingIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Identifiant de réservation invalide.");
        }

        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new BusinessException("Réservation introuvable.");
        }

        Ride ride = rideDao.findById(booking.getRideId());
        if (ride == null || ride.getDriverId() == null || !ride.getDriverId().equals(driver.getId())) {
            throw new BusinessException("Vous ne pouvez pas refuser cette réservation.");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Cette réservation n'est plus en attente.");
        }

        String msg = (message == null || message.isBlank())
                ? "Votre réservation a été refusée."
                : message.trim();

        bookingDao.updateBooking(bookingId, "REJECTED", msg, null);
    }
    
    
    /**
     * Nombre de places restantes sur un sous-trajet précis.
     * Lié à findByRideId(ObjectId rideId) dans BookingDao
     * fromIndex et toIndex sont des indices dans ride.getFullPath():
     *   - fromIndex = ville de départ du sous-trajet
     *   - toIndex   = ville d'arrivée du sous-trajet (exclu pour les segments)
     *
     * Exemple: fullPath = [Lyon, Valence, Marseille]
     *   • segment Lyon -> Valence: fromIndex = 0, toIndex = 1
     *   • segment Valence -> Marseille: 1, 2
     *   • trajet complet: 0, 2
     */
    public int getRemainingSeatsForSegment(Ride ride, int fromIndex,
                                           int toIndex) throws BusinessException {

        if (ride == null || ride.getId() == null) {
            throw new BusinessException("Trajet introuvable.");
        }

        List<String> path = ride.getFullPath();
        if (path == null || path.size() < 2) {
            return 0;
        }

        // nombre de segments 
        int nbSegments = path.size() - 1;

        // Validation des indices
        if (fromIndex < 0 || toIndex <= fromIndex || toIndex > nbSegments) {
            throw new BusinessException("Sous-trajet invalide.");
        }

        // used[seg] = nb de places occupées sur le segment seg
        int[] used = new int[nbSegments];

        // Récupère toutes les réservations de ce trajet
        List<Booking> allBookings = bookingDao.findByRideId(ride.getId());

        for (Booking b : allBookings) {
            String st = b.getStatus();

            // On ne compte que les réservations déjà CONFIRMÉES
            if (!"CONFIRMED".equalsIgnoreCase(st)) {
                continue;
            }

            int bf = b.getFromIndex();
            int bt = b.getToIndex();

            if (bf < 0 || bt <= bf || bt > nbSegments) {
                continue;
            }

            for (int seg = bf; seg < bt; seg++) {
                used[seg] += b.getSeats();
            }
        }

        // Sur le sous-trajet demandé, on regarde le segment le plus chargé
        int maxUsed = 0;
        for (int seg = fromIndex; seg < toIndex; seg++) {
            if (used[seg] > maxUsed) {
                maxUsed = used[seg];
            }
        }

        int remaining = ride.getTotalSeats() - maxUsed;
        return Math.max(remaining, 0);
    }

    
    /*
     * création d'une réservation sur un sous-trajet
     * 
     * */
    public Booking createBookingForSegment(User passenger, Ride ride, int fromIndex, int toIndex, int seats, int unitPrice) throws BusinessException
    {

    	if (passenger == null || passenger.getId() == null) { 
    		throw new BusinessException("Vous devez être connecté pour réserver.");
		}
			
    	if (ride == null || ride.getId() == null) {
			throw new BusinessException("Trajet introuvable.");
		}
		
    	// le conducteur ne peut pas réserver son propre trajet
        if (ride.getDriverId() != null
                && ride.getDriverId().equals(passenger.getId())) {
            throw new BusinessException("Tu ne peux pas réserver ton propre trajet.");
        }

        // pas de réservation dans le passé
        if (ride.getDepartureDateTime() != null &&
                ride.getDepartureDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Il n'est plus possible de réserver ce trajet (date dépassée).");
        }
    	
		// Validation indices
		List<String> path = ride.getFullPath();
		if (path == null || path.size() < 2) {
	        throw new BusinessException("Trajet invalide (pas assez d'étapes ou d'arret).");
	    }

		if (fromIndex < 0 || toIndex <= fromIndex || toIndex > path.size() - 1) {
			throw new BusinessException("Sous-trajet invalide.");
		 }
			
		if (seats <= 0) {
			throw new BusinessException("Nombre de places invalide.");
		}
		
		// Si aucun prix n'est fourni, on va chercher celui du sous-trajet
	    if (unitPrice <= 0) {
	        Integer segPrice = ride.getPricePerSeatForSegment(fromIndex, toIndex);
	        if (segPrice == null || segPrice <= 0) {
	            segPrice = ride.getPricePerSeat();
	        }
	        unitPrice = segPrice;
	    }
		
		// Vérifier les places disponible sur CE sous-trajet
		int remaining = getRemainingSeatsForSegment(ride, fromIndex, toIndex);
		if (seats > remaining) {
			throw new BusinessException("Plus assez de places disponibles sur ce sous-trajet.");
		}
		
		// Création de la réservation
		Booking booking = new Booking();
			
		booking.setRideId(ride.getId());
		booking.setPassengerId(passenger.getId());
		booking.setSeats(seats);
			
		booking.setFromIndex(fromIndex);
		booking.setToIndex(toIndex);
		booking.setFromCity(path.get(fromIndex));
		booking.setToCity(path.get(toIndex));
			
		booking.setPricePerSeat(unitPrice);   // prix/place sur ce sous-trajet
		booking.setTotalPrice(unitPrice * seats); // Prix total
			
		booking.setStatus("PENDING"); //
		booking.setCreatedAt(LocalDateTime.now());
		booking.setTicketCode(null);
		booking.setDriverMessage(null);
		
		bookingDao.insert(booking);

		
		return booking;
	}
 
}