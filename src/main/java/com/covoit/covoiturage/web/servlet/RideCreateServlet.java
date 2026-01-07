package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.service.RideService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Permet à un utilisateur connecté de créer un trajet.
 *  - GET: affiche le formulaire
 *  - POST: valide les données, appelle RideService, puis redirige vers la liste des trajets
 */

@WebServlet("/rides/new")
public class RideCreateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final RideService rideService = new RideService();
    
   // affiche un nouveau formulaire
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // AuthFilter protège déjà cette URL, donc ici on affiche juste le formulaire
        req.getRequestDispatcher("/WEB-INF/jsp/rides_new.jsp")
           .forward(req, resp);
    }
    
    // lit le formulaire, appelle RideService.createRide(...)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");
        
        // si l'utilisateur n'est pas connecté alors on le renvoie à la page login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Récupération des champs du formulaire
        String departureCity = req.getParameter("departureCity");
        String arrivalCity   = req.getParameter("arrivalCity");
        String dateStr       = req.getParameter("date"); // yyyy-MM-dd
        String timeStr       = req.getParameter("time"); // HH:mm
        String seatsStr      = req.getParameter("totalSeats");
        String priceStr      = req.getParameter("pricePerSeat");
        String description   = req.getParameter("description");
        String stopsRaw    = req.getParameter("stops"); // differents arrets saisis dans le textarea
        
        // prix par segment (générés par new_ride.js)
        String segmentPricesCsv = req.getParameter("segmentPricesCsv");
        
        try {
            // Parsing date/heure
            if (dateStr == null || timeStr == null) {
                throw new BusinessException("La date et l'heure de départ sont obligatoires.");
            }
            LocalDate date = LocalDate.parse(dateStr);      // format HTML5 date
            LocalTime time = LocalTime.parse(timeStr);      // format HTML5 time
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // ------  Parsing int pour les places et prix global    --------
            int totalSeats;
            int pricePerSeat;
            try {
                totalSeats = Integer.parseInt(seatsStr);
                pricePerSeat = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                throw new BusinessException("Nombre de places et prix doivent être des nombres entiers.");
            }
            
            
            // ----- Construction de la liste des villes pour connaître le nombre de segments -----
            
            // On reconstitue le chemin: départ, arrêts texte et arrivée
            List<String> stopsForSegments = new ArrayList<>();

            if (departureCity != null && !departureCity.isBlank()) {
                stopsForSegments.add(departureCity.trim());
            }
            
            if (stopsRaw != null && !stopsRaw.isBlank()) {
                // on coupe uniquement sur les sauts de ligne ici (comme dans le JS)
                String[] lines = stopsRaw.split("\\r?\\n");
                for (String line : lines) {
                    String city = line.trim();
                    if (!city.isEmpty()) {
                        stopsForSegments.add(city);
                    }
                }
            }
            
            
            if (arrivalCity != null && !arrivalCity.isBlank()) {
                String arrTrim = arrivalCity.trim();
                if (stopsForSegments.isEmpty()
                        || !arrTrim.equalsIgnoreCase(stopsForSegments.get(stopsForSegments.size() - 1))) {
                    stopsForSegments.add(arrTrim);
                }
            }
            
            // nombre de tronçons = nb villes - 1 (ex: Paris-Poitiers-Bordeaux-Madrid -> 3 segments)
            int nbSegments = Math.max(0, stopsForSegments.size() - 1);

            // ----- Parsing du CSV des prix de segments en List<Integer> -----
            List<Integer> segmentPrices = parseSegmentPrices(segmentPricesCsv, nbSegments);
            
            // ---------- Appel du service métier ----------
            rideService.createRide(
                    user,
                    departureCity,
                    arrivalCity,
                    dateTime,
                    totalSeats,
                    pricePerSeat,
                    description,
                    stopsRaw,     // le service reconstruit et normalise la liste d'arrêts
                    segmentPrices         
            );

            // Succès -> on se redirige vers la liste des trajets
            resp.sendRedirect(req.getContextPath() + "/rides");

        } catch (BusinessException e) {

            // En cas d'erreur, on renvoie sur le formulaire avec les valeurs déjà saisies
            req.setAttribute("error", e.getMessage());
            req.setAttribute("departureCity", departureCity);
            req.setAttribute("arrivalCity", arrivalCity);
            req.setAttribute("date", dateStr);
            req.setAttribute("time", timeStr);
            req.setAttribute("totalSeats", seatsStr);
            req.setAttribute("pricePerSeat", priceStr);
            req.setAttribute("description", description);
            req.setAttribute("stops", stopsRaw);
            
            req.setAttribute("segmentPricesCsv", segmentPricesCsv);

            req.getRequestDispatcher("/WEB-INF/jsp/rides_new.jsp")
               .forward(req, resp);
        }
    }
    
    
    /**
     * Transforme le CSV envoyé par le formulaire ("40,30,30")
     * en une liste de nbSegments éléments : [40, 30, 30].
     *
     * - Si le CSV est plus court, on complète avec null.
     * - Si une valeur n'est pas un entier, on met null.
     * - La liste a toujours exactement nbSegments éléments afin que
     *   Ride.getPricePerSeatForSegment(...) ne tombe pas dans le fallback.
     */
    private List<Integer> parseSegmentPrices(String csv, int nbSegments) {
        List<Integer> result = new ArrayList<>();
        
        if (nbSegments <= 0) {
            return result; // Si aucun tronçon -> liste vide
        }
        
        String[] parts;
        if (csv == null || csv.isBlank()) {
            parts = new String[0];
        } else {
            parts = csv.split(",");
        }
        
        for (int i = 0; i < nbSegments; i++) {
            Integer val = null;

            if (i < parts.length) {
                String s = parts[i].trim();
                if (!s.isEmpty()) {
                    try {
                        val = Integer.valueOf(s);
                    } catch (NumberFormatException e) {
                        // valeur invalide -> on laisse val = null
                    }
                }
            }

            // on ajoute même si c'est null pour garder la bonne taille
            result.add(val);
        }

        return result;
    }
}