package com.covoit.covoiturage;

import com.covoit.covoiturage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Page d'accueil simple.
 * Ne contient AUCUN HTML : il délègue l'affichage à /WEB-INF/jsp/hello.jsp
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // On récupère l'utilisateur en session (peut être null si non connecté)
        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof User) {
                user = (User) obj;
            }
        }

        // On peut poser l'utilisateur en attribut requête si on veut
        req.setAttribute("user", user);

        // On délègue l'affichage à la JSP
        req.getRequestDispatcher("/WEB-INF/jsp/hello.jsp")
           .forward(req, resp);
    }
}
