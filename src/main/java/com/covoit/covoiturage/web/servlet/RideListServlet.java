package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.service.RideService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Affiche la liste des trajets :
 *  - sans filtres: derniers trajets à venir
 *  - avec départ, arrivée, date: résultats de recherche
 */
@WebServlet("/rides")
public class RideListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final RideService rideService = new RideService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String departureCity = req.getParameter("departureCity");
        String arrivalCity   = req.getParameter("arrivalCity");
        String dateStr       = req.getParameter("date"); // yyyy-MM-dd

        List<Ride> rides;
        String error = null;
        boolean hasSearch = departureCity != null && !departureCity.isBlank()
                && arrivalCity != null && !arrivalCity.isBlank()
                && dateStr != null && !dateStr.isBlank();

        if (hasSearch) {
            // Cas recherche
            try {
                LocalDate date = LocalDate.parse(dateStr);
                rides = rideService.searchRides(departureCity, arrivalCity, date);
            } catch (BusinessException e) {
                error = e.getMessage();
                rides = Collections.emptyList();
            } catch (Exception e) {
                error = "Paramètres de recherche invalides.";
                rides = Collections.emptyList();
            }
        } else {
            // Cas liste simple (derniers trajets à venir)
            rides = rideService.getLatestRides(20);
        }

        req.setAttribute("rides", rides);
        req.setAttribute("error", error);

        // Renvoyer les critères pour les garder dans le formulaire
        req.setAttribute("departureCity", departureCity);
        req.setAttribute("arrivalCity", arrivalCity);
        req.setAttribute("date", dateStr);

        req.getRequestDispatcher("/WEB-INF/jsp/rides.jsp")
           .forward(req, resp);
    }
}
