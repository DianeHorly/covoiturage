package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.service.BookingService;
import com.covoit.covoiturage.service.RideService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Gère la réservation d'un trajet.
 *  - doGET: affiche le détail du trajet et formulaire "nombre de places"
 *  - doPOST: crée la réservation via BookingService
 *
 * URL: /book?rideId=...
 * Protégée par AuthFilter (l'utilisateur doit être connecté).
 */
@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

       
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // id du trajet
        String rideId = req.getParameter("rideId");
        if (rideId == null || rideId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/rides");
            return;
        }

        try {
            //  Récupérer le trajet
            Ride ride = rideService.getById(rideId);
            
            if (ride == null) {
                throw new BusinessException("Trajet introuvable.");
            }

            // Sous-trajet demandé par le passager (si on vient de /rides avec une recherche)
            String fromParam = req.getParameter("from");
            String toParam   = req.getParameter("to");  

            // chemin complet du trajet 
            List<String> fullPath = ride.getFullPath();  // méthode dans Ride
            req.setAttribute("fullPath", fullPath);
            
            
            Integer segmentFromIndex   = null;
            Integer segmentToIndex     = null;
            Integer segmentStopsCount  = null;
            String  segmentFromCity    = null;
            String  segmentToCity      = null;
            Integer segmentRemainingSeats = null;
            Integer segmentUnitPrice   = null;
            boolean isSubRide          = false;

            if (fullPath != null && !fullPath.isEmpty()) {

                // on construit une version "normalisée" du chemin (tout en minuscule)
                List<String> normPath = new ArrayList<>();
                for (String c : fullPath) {
                    String norm = Ride.normalizeCity(c); // trim et toLowerCase (la méthode statique)
                    if (norm != null) {
                        normPath.add(norm);
                    }
                }

                String fromNorm = Ride.normalizeCity(fromParam);
                String toNorm   = Ride.normalizeCity(toParam);

                if (fromNorm != null) {
                    int idx = normPath.indexOf(fromNorm);
                    if (idx >= 0) {
                        segmentFromIndex = idx;
                    }
                }
                if (toNorm != null) {
                    int idx = normPath.lastIndexOf(toNorm);
                    if (idx >= 0) {
                        segmentToIndex = idx;
                    }
                }

                // on vérifie que l'arrivée est bien après le départ
                if (segmentFromIndex != null && segmentToIndex != null && segmentToIndex > segmentFromIndex) {
                    
                	segmentFromCity   = fullPath.get(segmentFromIndex);
                    segmentToCity     = fullPath.get(segmentToIndex);
                    // nombre d'arrêts intermédiaires entre les deux
                    segmentStopsCount = segmentToIndex - segmentFromIndex - 1;
                    
                    // Verifie si c'est un sous-trajet
                    if (!(segmentFromIndex == 0 && segmentToIndex == fullPath.size() - 1)) {
                        isSubRide = true;
                    }

                    // places restantes sur ce sous-trajet
                    segmentRemainingSeats = bookingService.getRemainingSeatsForSegment(
                            ride,
                            segmentFromIndex,
                            segmentToIndex
                    );
                    
                    
                    Integer p = ride.getPricePerSeatForSegment(segmentFromIndex, segmentToIndex);
                    if (p != null && p > 0) {
                          segmentUnitPrice = p;
                        
                    }

                   
                } else {
                    // indice incohérent => on ignore ce sous-trajet
                    segmentFromIndex = null;
                    segmentToIndex   = null;
                }
            }
            
            // Calcul des places restantes globales (trajet complet)
            int remainingSeats = bookingService.getRemainingSeats(ride);

            // Variables pour la JSP
            req.setAttribute("ride", ride);
            req.setAttribute("remainingSeats", remainingSeats);
            req.setAttribute("rideId", rideId);

            // informations du sous-trajet éventuel du passager
            req.setAttribute("segmentFromCity", segmentFromCity);
            req.setAttribute("segmentToCity", segmentToCity);
            req.setAttribute("segmentStopsCount", segmentStopsCount);
            req.setAttribute("segmentFromIndex", segmentFromIndex);
            req.setAttribute("segmentToIndex", segmentToIndex);
            req.setAttribute("segmentRemainingSeats", segmentRemainingSeats);
            req.setAttribute("segmentUnitPrice", segmentUnitPrice);
            req.setAttribute("isSubRide", isSubRide);

            //redirige vers la JSP
            req.getRequestDispatcher("/WEB-INF/jsp/booking.jsp")
               .forward(req, resp);

        } catch (BusinessException e) {
            // Erreur (trajet introuvable, id invalide, etc.)
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/booking.jsp")
               .forward(req, resp);
        }
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String rideId = req.getParameter("rideId");
        String seatsStr = req.getParameter("seats");
        String fromIndexStr = req.getParameter("fromIndex");
        String toIndexStr = req.getParameter("toIndex");
        String unitPriceStr = req.getParameter("unitPrice");

        try {
        	if (rideId == null || rideId.isBlank()) {
                throw new BusinessException("Trajet manquant.");
            }
            if (seatsStr == null || seatsStr.isBlank()) {
                throw new BusinessException("Merci d'indiquer le nombre de places.");
            }

            int seats = Integer.parseInt(seatsStr);

            // Récupère le trajet
            Ride ride = rideService.getById(rideId);
            if (ride == null) {
                throw new BusinessException("Trajet introuvable.");
            }
            
            List<String> fullPath = ride.getFullPath();

            int fromIndex = 0;
            int toIndex   = fullPath.size() - 1;

            // Si la JSP a envoyé des indices spécifiques de sous-trajet
            if (fromIndexStr != null && !fromIndexStr.isBlank()) {
                fromIndex = Integer.parseInt(fromIndexStr);
            }
            if (toIndexStr != null && !toIndexStr.isBlank()) {
                toIndex = Integer.parseInt(toIndexStr);
            }

            // Prix / place pour ce sous-trajet
            int unitPrice;
            if (unitPriceStr != null && !unitPriceStr.isBlank()) {
                // la JSP a envoyé un prix explicite
                unitPrice = Integer.parseInt(unitPriceStr);
            } else {
            	// on calcule via les segments (ou on retombe sur pricePerSeat)
                int p = ride.computeUnitPriceForSegment(fromIndex, toIndex);
                if (p <= 0) {
                    p = ride.getPricePerSeat();
                }
                unitPrice = p;
            }

            // Création de la réservation (complète ou sous-trajet)
            Booking booking = bookingService.createBookingForSegment(
                    user,
                    ride,
                    fromIndex,
                    toIndex,
                    seats,
                    unitPrice
            );

            // Message flash
            String fromCity = (booking.getFromCity() != null) ? booking.getFromCity() : ride.getDepartureCityDisplay();
            String toCity   = (booking.getToCity()   != null) ? booking.getToCity()   : ride.getArrivalCityDisplay();

            session.setAttribute("flashMessage",
                    "Réservation effectuée pour le trajet "
                            + fromCity + " → " + toCity + ".");

            resp.sendRedirect(req.getContextPath() + "/myBookings");
            return;
                
            
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Les valeurs numériques sont invalides (places ou prix).");
            
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            
        }

        // En cas d'erreur: on réaffiche la page de réservation
        try {
            Ride ride = rideService.getById(rideId);
            int remainingSeats = bookingService.getRemainingSeats(ride);

            req.setAttribute("ride", ride);
            req.setAttribute("remainingSeats", remainingSeats);
            req.setAttribute("rideId", rideId);
        } catch (BusinessException ignored) {
            // on ignore
        } 

        req.getRequestDispatcher("/WEB-INF/jsp/booking.jsp")
           .forward(req, resp);
    }
}
