package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.service.RideService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;


/**
 * Page d'accueil de l'application
 *  Elle délègue ou est lié à /WEB-INF/jsp/home.jsp
 */
@WebServlet({"/home"})
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RideService rideService = new RideService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // On peut récupérer l'utilisateur de la session si besoin
        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof User) {
                user = (User) obj;
            }
        }

        // On pose l'utilisateur en attribut de la requête 
        req.setAttribute("currentUser", user);

       // Quelques trajets récents pour la page d'accueil
        List<Ride> latestRides = rideService.getLatestRides(6);
        req.setAttribute("latestRides", latestRides);
        
        // On redirige à la JSP home
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp")
           .forward(req, resp);
    }
}
