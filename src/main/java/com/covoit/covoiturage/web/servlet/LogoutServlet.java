package com.covoit.covoiturage.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * chemin de Déconnexion de l'utilisateur :
 *  - invalide la session
 *  - redirige vers /home
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false); // false -> ne crée pas de nouvelle session
        if (session != null) {
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
