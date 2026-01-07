// Servlet de connexion

package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.service.UserService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Gère la connexion d'un utilisateur existant.
 * - GET: affiche le formulaire de login
 * - POST: appelle UserService.login(...) et stocke l'utilisateur en session
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Afficher le formulaire de connexion
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            // Vérifie les identifiants côté service
            User user = userService.login(email, password);

            // Si OK : on crée / récupère la session
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);

            // Redirection vers la page d'accueil
            resp.sendRedirect(req.getContextPath() + "/home");

        } catch (BusinessException e) {
            // Identifiants invalides ou champs manquants
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
               .forward(req, resp);
        }
    }
}