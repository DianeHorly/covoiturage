// Servlet d'inscription

package com.covoit.covoiturage.web.servlet;


//import com.covoit.covoiturage.dao.UserDao;
import com.covoit.covoiturage.service.UserService;
import com.covoit.covoiturage.util.BusinessException;
import com.covoit.covoiturage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gère l'inscription d'un nouvel utilisateur.
 * - GET: affiche le formulaire d'inscription register.jsp
 * - POST: vérifie les champs, enregistre l'utilisateur en base (MongoDB),
 * 		   appelle UserService.register(...) puis redirige vers /login si succès
 */
@WebServlet("/register")        // l'URL de ce servlet sera /covoiturage/register
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Service métier pour gérer les utilisateurs (validation et MongoDB)
    private final UserService userService = new UserService();

    
    /**
     * Affiche le formulaire d'inscription.
     * Appelé quand on va sur /register en GET .
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // affiche la JSP d'inscription
        req.getRequestDispatcher("/WEB-INF/jsp/register.jsp")
           .forward(req, resp);
    }

    /**
     * Traite la soumission du formulaire d'inscription.
     * Appelé quand le formulaire /register.jsp fait un POST vers /register.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Encodage des paramètres en UTF-8 (accents, etc.)
        req.setCharacterEncoding("UTF-8");

        // Récupération des paramètres du formulaire
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            // On délègue la logique métier à UserService
            User user = userService.register(name, email, password);

            //  Si tout s'est bien passé, on redirige vers la page de connexion /login
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (BusinessException e) {
            // Erreur métier (champs vides, email déjà utilisé, etc.)
            // On renvoie sur le formulaire avec un message d'erreur
            req.setAttribute("error", e.getMessage());
            
            // On renvoie aussi les valeurs saisies pour pré-remplir le formulaire et éviter à l'utilisateur
            // de tout retaper.
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            
            // On renvoie vers la page d'inscription
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp")
               .forward(req, resp);
        }
    }
}
