package com.covoit.covoiturage.web.filter;

import com.covoit.covoiturage.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtre d'authentification.
 *
 * Il s'applique sur les URL qui doivent être protégées.
 * Si l'utilisateur n'est pas connecté (pas de "user" en session),
 * on le redirige vers /login.
 * * Laisse l'accès PUBLIC à /home, /rides, etc.
 * Ne protège QUE les URL qui nécessitent d'être connecté :
 *  - /rides/new: proposer un trajet
 *  - /book, /book/*: réserver un trajet (plus tard)
 *  - /myBookings: voir mes réservations (plus tard)
 */


@WebFilter({
        "/rides/new",
        "/book", 
        "/book/*",
        "/myBookings"
        //"/driver/*"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            // Si Pas connecté -> redirection vers la page de connexion
            // On pourrait ajouter un paramètre redirect pour revenir ensuite.
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Utilisateur connecté -> on laisse passer la requête
        chain.doFilter(request, response);
    }
}
